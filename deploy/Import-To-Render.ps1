param(
  [Parameter(Mandatory=$true)][string]$ExternalDatabaseUrl,
  [string]$InputFile = (Join-Path $PSScriptRoot 'barber.dump')
)
$ErrorActionPreference = 'Stop'
if (-not (Test-Path -LiteralPath $InputFile)) { throw "Dump file not found: $InputFile" }
if (-not (Get-Command pg_restore -ErrorAction SilentlyContinue)) { throw 'pg_restore was not found. Install PostgreSQL client tools and add the bin folder to PATH.' }
& pg_restore --dbname=$ExternalDatabaseUrl --no-owner --no-acl --clean --if-exists $InputFile
if ($LASTEXITCODE -ne 0) { throw "pg_restore failed with exit code $LASTEXITCODE." }
Write-Host 'Barber database transfer completed.'
