param([string]$JdkHome='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot')
$ErrorActionPreference='Stop'
& "$PSScriptRoot\Build.ps1" -JdkHome $JdkHome
New-Item -ItemType Directory "$PSScriptRoot\build\tests" -Force | Out-Null
$tests=@(Get-ChildItem "$PSScriptRoot\tests" -Recurse -Filter '*.java' | ForEach-Object FullName)
& "$JdkHome\bin\javac.exe" --release 21 -encoding UTF-8 -cp "$PSScriptRoot\build\classes" -d "$PSScriptRoot\build\tests" @tests
if($LASTEXITCODE -ne 0){throw 'Test compilation failed.'}
& "$JdkHome\bin\java.exe" -cp "$PSScriptRoot\build\classes;$PSScriptRoot\build\tests" com.barber.UnitTests
if($LASTEXITCODE -ne 0){throw 'Unit tests failed.'}
