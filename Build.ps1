param([string]$JdkHome='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot',[string]$TomcatHome="$PSScriptRoot\.local\runtime\apache-tomcat-11.0.25")
$ErrorActionPreference='Stop'
& "$PSScriptRoot\Prepare-Views.ps1"
$classes=Join-Path $PSScriptRoot 'build\classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$sources=@(Get-ChildItem "$PSScriptRoot\src" -Recurse -Filter '*.java' | ForEach-Object FullName)
& "$JdkHome\bin\javac.exe" --release 21 -encoding UTF-8 -cp "$TomcatHome\lib\servlet-api.jar" -d $classes @sources
if($LASTEXITCODE -ne 0){throw 'Java compilation failed.'}
Write-Output 'Barber compilation succeeded.'
