$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing backend/.env. Copy backend/.env.example to backend/.env first."
}

foreach ($line in Get-Content -LiteralPath $envFile) {
    $trimmedLine = $line.Trim()
    if (-not $trimmedLine -or $trimmedLine.StartsWith("#")) {
        continue
    }

    $name, $value = $trimmedLine.Split("=", 2)
    if ($name -notmatch "^[A-Za-z_][A-Za-z0-9_]*$") {
        throw "Invalid environment variable name in backend/.env: $name"
    }

    [Environment]::SetEnvironmentVariable($name, $value, "Process")
}

Push-Location $PSScriptRoot
try {
    & ".\mvnw.cmd" spring-boot:run
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
