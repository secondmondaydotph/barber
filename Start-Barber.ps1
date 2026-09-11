param(
 [int]$Port=8082,
 [string]$JdkHome='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot',
 [string]$TomcatHome="$PSScriptRoot\.local\runtime\apache-tomcat-11.0.25",
 [switch]$SkipBuild
)
$ErrorActionPreference='Stop'
if($Port -lt 1024 -or $Port -gt 65535){throw 'Invalid local port.'}
$probe=[Net.Sockets.TcpClient]::new()
try{
 $probe.Connect('127.0.0.1',$Port)
 try{$health=Invoke-WebRequest "http://127.0.0.1:$Port/Barber/health" -UseBasicParsing -TimeoutSec 5}catch{$health=$null}
 if($health -and $health.StatusCode -eq 200){Write-Output "Barber is already running: http://localhost:$Port/Barber/";exit 0}
 throw "Port $Port is occupied by another application. Choose another -Port."
}catch [Net.Sockets.SocketException]{}finally{$probe.Dispose()}
if(-not $SkipBuild){& "$PSScriptRoot\Build.ps1" -JdkHome $JdkHome -TomcatHome $TomcatHome}
$base=Join-Path $PSScriptRoot '.local\server'
foreach($dir in @('conf','logs','temp','work','webapps')){New-Item -ItemType Directory -Path "$base\$dir" -Force | Out-Null}
Copy-Item "$TomcatHome\conf\*" "$base\conf" -Recurse -Force
Copy-Item "$PSScriptRoot\server.xml" "$base\conf\server.xml" -Force
[xml]$xml=Get-Content -Raw "$base\conf\server.xml";$xml.Server.Service.Connector.SetAttribute('port',[string]$Port);$xml.Save("$base\conf\server.xml")
$app="$base\webapps\Barber"
if(-not $SkipBuild){
 New-Item -ItemType Directory -Path "$app\WEB-INF\classes" -Force | Out-Null
 Copy-Item "$PSScriptRoot\WebContent\*" $app -Recurse -Force
 Copy-Item "$PSScriptRoot\build\classes\*" "$app\WEB-INF\classes" -Recurse -Force
}elseif(-not (Test-Path "$app\WEB-INF\web.xml")){throw 'The deployed Barber application is missing. Run Start-Barber.ps1 once without -SkipBuild.'}
$config=Join-Path $PSScriptRoot '.local\database.properties'
$javaArgs=@("`"-Dcatalina.home=$TomcatHome`"","`"-Dcatalina.base=$base`"","`"-Djava.io.tmpdir=$base\temp`"","`"-Dbarber.config=$config`"",'-classpath',"`"$TomcatHome\bin\bootstrap.jar;$TomcatHome\bin\tomcat-juli.jar`"",'org.apache.catalina.startup.Bootstrap','start')
$p=Start-Process "$JdkHome\bin\java.exe" -ArgumentList $javaArgs -WorkingDirectory $base -WindowStyle Hidden -PassThru -RedirectStandardOutput "$base\logs\console.out.log" -RedirectStandardError "$base\logs\console.err.log"
[IO.File]::WriteAllText((Join-Path $PSScriptRoot '.local\barber.pid'),[string]$p.Id)
$ready=$false
for($attempt=0;$attempt -lt 30;$attempt++){
 Start-Sleep -Milliseconds 500
 try{$response=Invoke-WebRequest "http://127.0.0.1:$Port/Barber/health" -UseBasicParsing -TimeoutSec 2;if($response.StatusCode -eq 200){$ready=$true;break}}catch{}
}
if(-not $ready){throw "Barber did not become ready. Check $base\logs\console.err.log"}
Write-Output "Barber process $($p.Id) is ready: http://localhost:$Port/Barber/"
Write-Output 'This server serves only Barber and does not change your Eclipse server.'
