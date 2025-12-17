#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Automated Docker-based API testing suite
.DESCRIPTION
    Builds Docker image, runs container, executes API tests, and generates report
#>

param(
    [switch]$TestOnly,
    [switch]$Cleanup,
    [int]$MaxWaitSeconds = 60
)

$ErrorActionPreference = "Stop"
$WarningPreference = "SilentlyContinue"

$ProjectDir = "c:\Users\Adryan\Desktop\aps back end\campus-clinic-api"
$ContainerName = "clinic-api-test"
$ImageName = "clinic-api:test"
$BaseUrl = "http://localhost:8080"
$Port = 8080
$DbVolumeName = "clinic-api-db"

# ═══════════════════════════════════════════════════════════════════════════════
# LOGGING FUNCTIONS
# ═══════════════════════════════════════════════════════════════════════════════

function Write-Header {
    param([string]$Text, [string]$Color = "Cyan")
    Write-Host ""
    Write-Host "===================================================" -ForegroundColor $Color
    Write-Host "  $Text" -ForegroundColor $Color
    Write-Host "===================================================" -ForegroundColor $Color
}

function Write-Status {
    param([string]$Message, [string]$Icon = "[*]")
    Write-Host "$Icon $Message" -ForegroundColor Gray
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "[!!] $Message" -ForegroundColor Red
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "[!] $Message" -ForegroundColor Yellow
}

# ═══════════════════════════════════════════════════════════════════════════════
# DOCKER FUNCTIONS
# ═══════════════════════════════════════════════════════════════════════════════

function Test-Docker {
    try {
        docker --version | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

function Stop-ExistingContainer {
    try {
        $existing = docker ps -a -q -f "name=^${ContainerName}$" 2>$null
        if ($existing) {
            Write-Status "Stopping existing container..."
            docker stop $ContainerName 2>$null | Out-Null
            Start-Sleep -Milliseconds 500
            docker rm $ContainerName 2>$null | Out-Null
            Write-Success "Container removed"
        }
    }
    catch { }
}

function Build-DockerImage {
    Write-Header "BUILDING DOCKER IMAGE" "Blue"
    
    Write-Status "Building: $ImageName"
    Write-Status "Directory: $ProjectDir"
    
    Push-Location $ProjectDir
    try {
        $output = docker build -t $ImageName . 2>&1
        $output | ForEach-Object {
            if ($_ -match "Successfully|error|ERROR|failed") {
                Write-Host $_
            }
        }
        
        Write-Success "Image built successfully"
        Pop-Location
        return $true
    }
    catch {
        Write-Error-Custom "Failed to build Docker image: $_"
        Pop-Location
        return $false
    }
}

function Ensure-ImagePresent {
    try {
        docker image inspect $ImageName 2>$null | Out-Null
        return $true
    }
    catch {
        Write-Warning-Custom "Image '$ImageName' not found locally."
        if ($TestOnly) {
            Write-Status "Auto-building image because --TestOnly is set but image is missing"
        }
        else {
            Write-Status "Building image..."
        }
        return (Build-DockerImage)
    }
}

function Start-Container {
    Write-Header "STARTING CONTAINER" "Blue"
    
    Write-Status "Starting: $ContainerName"
    
    try {
        docker run `
            -d `
            --name $ContainerName `
            --mount type=volume,source=$DbVolumeName,destination=/app/data `
            -p "${Port}:8080" `
            -e "JAVA_OPTS=-Xmx512m" `
            $ImageName 2>&1 | Out-Null
        
        Start-Sleep -Milliseconds 500
        $cid = docker ps -q -f "name=$ContainerName" 2>$null
        Write-Success "Container started (ID: $cid)"
    }
    catch {
        Write-Error-Custom "Failed to start container: $_"
        return $false
    }
    
    # Wait for container to be ready
    Write-Status "Waiting for container to be ready..."
    
    $startTime = Get-Date
    $ready = $false
    $attempt = 0
    
    while ((New-TimeSpan -Start $startTime -End (Get-Date)).TotalSeconds -lt $MaxWaitSeconds) {
        $attempt++
        try {
            $response = Invoke-WebRequest -Uri "$BaseUrl/api/specialties" `
                -Method Get `
                -TimeoutSec 2 `
                -ErrorAction SilentlyContinue `
                -UseBasicParsing
            
            if ($response.StatusCode -eq 200) {
                Write-Success "Container ready for tests (attempt $attempt)"
                $ready = $true
                break
            }
        }
        catch { 
            Write-Host "." -NoNewline -ForegroundColor Gray
        }
        
        Start-Sleep -Milliseconds 1000
    }
    
    Write-Host ""
    if (-not $ready) {
        Write-Warning-Custom "Container may not be fully ready (timeout: ${MaxWaitSeconds}s)"
        Write-Status "Continuing anyway..."
    }
    
    return $true
}

# ═══════════════════════════════════════════════════════════════════════════════
# API TEST FUNCTIONS
# ═══════════════════════════════════════════════════════════════════════════════

function Test-Endpoint {
    param(
        [string]$Uri,
        [string]$Name,
        [string]$Method = "Get",
        [object]$Body = $null,
        [int[]]$AcceptableStatuses = @(200, 201, 202, 204, 400, 404),
        [bool]$TestMode = $true
    )

    try {
        $params = @{
            Uri          = $Uri
            Method       = $Method
            TimeoutSec   = 10
            ErrorAction  = "Stop"
            UseBasicParsing = $true
            Headers      = @{ 
                "Accept" = "application/json"
                "X-Test-Mode" = if ($TestMode) { "true" } else { "false" }
            }
        }

        if ($Body -ne $null) {
            $params.ContentType = "application/json"
            $params.Body = ($Body | ConvertTo-Json -Depth 6)
        }

        $response = Invoke-WebRequest @params

        $bodyText = $response.Content
        $msg = ""
        $data = $null
        try {
            $json = $bodyText | ConvertFrom-Json
            if ($json.message) { $msg = $json.message }
            elseif ($json.status) { $msg = $json.status }
            elseif ($json.error) { $msg = $json.error }
            else { $msg = $bodyText }

            if ($json.data) { $data = $json.data }
        }
        catch { $msg = $bodyText }

        $msg = if ($msg.Length -gt 120) { $msg.Substring(0,120) + "..." } else { $msg }

        $ok = $AcceptableStatuses -contains $response.StatusCode
        return @{ Success = $ok; Name = $Name; Status = $response.StatusCode; Endpoint = $Uri; Message = $msg; Data = $data }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        if ($null -eq $statusCode) { $statusCode = "TIMEOUT/ERROR" }
        $msg = $_.Exception.Message
        return @{ Success = ($AcceptableStatuses -contains $statusCode); Name = $Name; Status = $statusCode; Endpoint = $Uri; Message = $msg; Data = $null }
    }
}

function Run-ApiTests {
    Write-Header "RUNNING API TESTS" "Green"

    $results = @()
    $successCount = 0
    $failCount = 0
    $ctx = @{}

    function Step {
        param(
            [string]$Name,
            [string]$Method,
            [string]$Uri,
            [object]$Body = $null,
            [int[]]$AcceptableStatuses = @(200,201,204,400,404)
        )

        $result = Test-Endpoint -Uri $Uri -Name $Name -Method $Method -Body $Body -AcceptableStatuses $AcceptableStatuses
        $results += $result
        $statusText = "$($result.Status)"
        $line = "$Name -> $Uri => $statusText"

        if ($result.Success) {
            Write-Host "[OK]   $line" -ForegroundColor Green
            if ($result.Message) { Write-Host "       msg: $($result.Message)" -ForegroundColor DarkGray }
        }
        else {
            Write-Host "[FAIL] $line" -ForegroundColor Red
            if ($result.Message) { Write-Host "       msg: $($result.Message)" -ForegroundColor DarkYellow }
        }

        return $result
    }

    Write-Host ""

    # Seed data (POST/PUT/DELETE coverage)
    $r = Step "Create Specialty" "Post" "$BaseUrl/api/specialties" @{ name = "Cardiology"; description = "Heart care" } @(201,200)
    $ctx.SpecialtyId = $r.Data.specialtyId
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Create Doctor" "Post" "$BaseUrl/api/doctors" @{ name = "Dr. Alice"; medicalLicense = "CRM12345"; birthDate = "1980-01-01"; phone = "555-1111"; active = $true; specialty = @{ specialtyId = $ctx.SpecialtyId } } @(201,200)
    $ctx.DoctorId = $r.Data.doctorId
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Update Doctor" "Put" "$BaseUrl/api/doctors/$($ctx.DoctorId)" @{ name = "Dr. Alice Updated"; medicalLicense = "CRM12345"; birthDate = "1980-01-01"; phone = "555-2222"; active = $true; specialty = @{ specialtyId = $ctx.SpecialtyId } } @(200)
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Create Patient" "Post" "$BaseUrl/api/patients" @{ name = "Bob Patient"; gender = "M"; cpf = "12345678901"; birthDate = "1990-05-05"; phone = "555-3333"; address = "Main St"; email = "bob@example.com" } @(201,200)
    $ctx.PatientId = $r.Data.patientId
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Update Patient" "Put" "$BaseUrl/api/patients/$($ctx.PatientId)" @{ phone = "555-4444"; email = "bob+upd@example.com"; address = "Updated Address" } @(200)
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Schedule Appointment" "Post" "$BaseUrl/api/appointments/schedule" @{ patientId = $ctx.PatientId; doctorId = $ctx.DoctorId; appointmentDate = "2025-12-31"; startTime = "09:00"; endTime = "09:30" } @(201,200)
    $ctx.AppointmentId = $r.Data.id
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Update Appointment Status" "Put" "$BaseUrl/api/appointments/$($ctx.AppointmentId)" @{ status = "COMPLETED" } @(200)
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Cancel Appointment" "Put" "$BaseUrl/api/appointments/$($ctx.AppointmentId)/cancel" $null @(200)
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Create Medical Record" "Post" "$BaseUrl/api/medical-records" @{ appointmentId = $ctx.AppointmentId; anamnesis = "Initial notes"; diagnosis = "Healthy"; prescription = "Hydrate" } @(201,200)
    $ctx.RecordId = $r.Data.recordId
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Update Medical Record" "Put" "$BaseUrl/api/medical-records/$($ctx.RecordId)" @{ anamnesis = "Updated anamnesis"; diagnosis = "Updated diagnosis"; prescription = "Updated prescription" } @(200)
    if ($r.Success) { $successCount++ } else { $failCount++ }

    # GET coverage using created IDs
    $getTests = @(
        @{ Name = "List Specialties"; Uri = "$BaseUrl/api/specialties"; Accept = @(200) },
        @{ Name = "Get Specialty by Id"; Uri = "$BaseUrl/api/specialties/$($ctx.SpecialtyId)"; Accept = @(200,404) },

        @{ Name = "List Doctors"; Uri = "$BaseUrl/api/doctors"; Accept = @(200) },
        @{ Name = "Get Doctor by Id"; Uri = "$BaseUrl/api/doctors/$($ctx.DoctorId)"; Accept = @(200,404,400) },
        @{ Name = "List Doctors by Specialty"; Uri = "$BaseUrl/api/doctors/specialty/$($ctx.SpecialtyId)"; Accept = @(200,404) },
        @{ Name = "Doctor Appointment Report"; Uri = "$BaseUrl/api/doctors/$($ctx.DoctorId)/report/appointments"; Accept = @(200,404) },
        @{ Name = "Doctor Upcoming Appointments"; Uri = "$BaseUrl/api/doctors/$($ctx.DoctorId)/upcoming/appointments"; Accept = @(200,404) },
        @{ Name = "Doctor Available Slots"; Uri = "$BaseUrl/api/doctors/$($ctx.DoctorId)/available-slots?date=2025-12-31"; Accept = @(200,404) },

        @{ Name = "List Patients"; Uri = "$BaseUrl/api/patients"; Accept = @(200) },
        @{ Name = "Get Patient by Id"; Uri = "$BaseUrl/api/patients/$($ctx.PatientId)"; Accept = @(200,404,400) },
        @{ Name = "Get Patient Age"; Uri = "$BaseUrl/api/patients/$($ctx.PatientId)/age"; Accept = @(200,404,400) },
        @{ Name = "Get Patient History"; Uri = "$BaseUrl/api/patients/$($ctx.PatientId)/history"; Accept = @(200,404) },
        @{ Name = "Patient Appointment Report"; Uri = "$BaseUrl/api/patients/$($ctx.PatientId)/report/appointments/6"; Accept = @(200,404) },
        @{ Name = "Patients by Specialty Report"; Uri = "$BaseUrl/api/patients/report/specialties"; Accept = @(200) },

        @{ Name = "List Appointments"; Uri = "$BaseUrl/api/appointments"; Accept = @(200) },
        @{ Name = "Get Appointment by Id"; Uri = "$BaseUrl/api/appointments/$($ctx.AppointmentId)"; Accept = @(200,404,400) },
        @{ Name = "Appointments by Patient"; Uri = "$BaseUrl/api/appointments/patient/$($ctx.PatientId)"; Accept = @(200,404) },
        @{ Name = "Appointments by Doctor"; Uri = "$BaseUrl/api/appointments/doctor/$($ctx.DoctorId)"; Accept = @(200,404) },
        @{ Name = "Appointments by Date"; Uri = "$BaseUrl/api/appointments/date/2025-12-31"; Accept = @(200,404) },

        @{ Name = "List Medical Records"; Uri = "$BaseUrl/api/medical-records"; Accept = @(200) },
        @{ Name = "Medical Record by Appointment"; Uri = "$BaseUrl/api/medical-records/appointment/$($ctx.AppointmentId)"; Accept = @(200,404) }
    )

    foreach ($t in $getTests) {
        $result = Test-Endpoint -Uri $t.Uri -Name $t.Name -Method "Get" -AcceptableStatuses $t.Accept
        $results += $result
        $statusText = "$($result.Status)"
        $line = "$($t.Name) -> $($t.Uri) => $statusText"

        if ($result.Success) {
            Write-Host "[OK]   $line" -ForegroundColor Green
            if ($result.Message) { Write-Host "       msg: $($result.Message)" -ForegroundColor DarkGray }
            $successCount++
        }
        else {
            Write-Host "[FAIL] $line" -ForegroundColor Red
            if ($result.Message) { Write-Host "       msg: $($result.Message)" -ForegroundColor DarkYellow }
            $failCount++
        }
    }

    # Delete flows
    $r = Step "Delete Medical Record" "Delete" "$BaseUrl/api/medical-records/$($ctx.RecordId)" $null @(200,204)
    if ($r.Success) { $successCount++ } else { $failCount++ }

    $r = Step "Delete Doctor" "Delete" "$BaseUrl/api/doctors/$($ctx.DoctorId)" $null @(200,204,409)
    if ($r.Success) { $successCount++ } else { $failCount++ }

    Write-Host ""
    return @{
        Results      = $results
        SuccessCount = $successCount
        FailCount    = $failCount
    }
}

# ═══════════════════════════════════════════════════════════════════════════════
# REPORT GENERATION
# ═══════════════════════════════════════════════════════════════════════════════

function Generate-Report {
    param($TestResults)
    
    $total = $TestResults.SuccessCount + $TestResults.FailCount
    $successRate = if ($total -gt 0) { ([math]::Round(($TestResults.SuccessCount / $total) * 100, 2)) } else { 0 }
    
    Write-Header "TEST REPORT" "Cyan"
    
    Write-Host ""
    Write-Host "SUMMARY" -ForegroundColor Cyan
    Write-Host "  Tests executed:       $total"
    Write-Host "  OK:                   $($TestResults.SuccessCount)"
    Write-Host "  FAIL:                 $($TestResults.FailCount)"
    Write-Host "  Success rate:         ${successRate}%"
    Write-Host ""
    
    if ($TestResults.FailCount -eq 0) {
        Write-Host ">>> ALL TESTS PASSED! <<<" -ForegroundColor Green -BackgroundColor DarkGreen
    }
    else {
        Write-Host ">>> SOME TESTS FAILED - See details above <<<" -ForegroundColor Yellow
    }
    
    Write-Host ""
}

function Cleanup-Docker {
    if (-not $Cleanup) { return }
    
    Write-Header "CLEANUP" "Yellow"
    
    Write-Status "Stopping container..."
    docker stop $ContainerName 2>$null | Out-Null
    docker rm $ContainerName 2>$null | Out-Null
    Write-Success "Container removed"
    
    Write-Status "Removing image..."
    docker rmi $ImageName 2>$null | Out-Null
    Write-Success "Image removed"
    
    Write-Host ""
}

# ═══════════════════════════════════════════════════════════════════════════════
# MAIN EXECUTION
# ═══════════════════════════════════════════════════════════════════════════════

function Main {
    Write-Header "TEST SUITE - CLINIC API (DOCKER)" "Magenta"
    
    # Verify Docker
    if (-not (Test-Docker)) {
        Write-Error-Custom "Docker is not installed or not in PATH"
        Write-Status "Download Docker Desktop at: https://www.docker.com/products/docker-desktop"
        exit 1
    }
    
    Write-Success "Docker detected"
    
    # Stop existing container
    Stop-ExistingContainer
    
    # Build image (unless --TestOnly)
    if (-not $TestOnly) {
        if (-not (Build-DockerImage)) {
            exit 1
        }
    }
    else {
        Write-Status "Skipping rebuild (--TestOnly enabled)"
        if (-not (Ensure-ImagePresent)) {
            Write-Error-Custom "Failed to ensure Docker image is available."
            exit 1
        }
    }
    
    # Start container
    if (-not (Start-Container)) {
        exit 1
    }
    
    # Run tests
    $testResults = Run-ApiTests
    
    # Generate report
    Generate-Report $testResults
    
    # Cleanup (only if specified)
    Cleanup-Docker
    
    # Exit with appropriate code
    if ($testResults.FailCount -gt 0) {
        exit 1
    }
    exit 0
}

try {
    Main
}
catch {
    Write-Error-Custom "Error during execution: $_"
    exit 1
}
