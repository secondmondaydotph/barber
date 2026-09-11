param(
  [string]$Database = 'barber',
  [string]$HostName = 'localhost',
  [int]$Port = 5432,
  [string]$User = 'postgres',
  [string]$Output = (Join-Path $PSScriptRoot 'barber.dump')
)
$ErrorActionPreference = 'Stop'
if (-not (Get-Command pg_dump -ErrorAction SilentlyContinue)) { throw 'pg_dump was not found. Install PostgreSQL client tools and add the bin folder to PATH.' }
& pg_dump --host=$HostName --port=$Port --username=$User --format=custom --no-owner --no-acl --file=$Output $Database
if ($LASTEXITCODE -ne 0) { throw "pg_dump failed with exit code $LASTEXITCODE." }
Write-Host "Created $Output"
