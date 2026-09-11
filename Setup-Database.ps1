param([string]$JdkHome='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot')
$ErrorActionPreference='Stop'
$config="$PSScriptRoot\.local\database.properties"
if(!(Test-Path -LiteralPath $config)){throw 'Copy database/database.properties.example to .local/database.properties and enter your LOCAL PostgreSQL password there first. Do not commit that file.'}
& "$PSScriptRoot\Build.ps1" -JdkHome $JdkHome
& "$JdkHome\bin\java.exe" "-Dbarber.config=$config" -cp "$PSScriptRoot\build\classes;$PSScriptRoot\WebContent\WEB-INF\lib\postgresql.jar" com.barber.util.SetupDatabase $PSScriptRoot
if($LASTEXITCODE -ne 0){throw 'Database setup failed. Check local credentials and database-create permission.'}
