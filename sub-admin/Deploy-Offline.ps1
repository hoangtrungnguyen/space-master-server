# Deploy-Offline.ps1
#
# DESCRIPTION:
# This script automates the offline deployment of the Spring Boot application.
# It assumes all necessary installers (Java, PostgreSQL) and the application JAR
# are located within a structured folder relative to this script.
#
# PRE-REQUISITES:
# 1. Run this script with Administrator privileges.
# 2. The folder structure should be:
#    /
#    ├── installers/
#    │   ├── postgresql-*.exe
#    │   └── openjdk-*.zip
#    ├── app/
#    │   └── *.jar
#    └── Deploy-Offline.ps1
#
# ===================================================================
# ===================================================================
# CONFIGURATION
# ===================================================================
# --- Application Configuration ---
$appName = "sub-admin"
# Assumes the JAR is the only one in the 'app' sub-directory
$jarFile = (Get-ChildItem -Path (Join-Path $PSScriptRoot "app") -Filter *.jar)[0]

# --- Database Configuration (from application.properties) ---
$dbName = "sub_admin_db"
$dbUser = "postgres"
$dbPassword = "postgres" # This password will be set for the PostgreSQL user during installation.
$dbPort = 5439

# --- Installer Configuration ---
$installersPath = Join-Path $PSScriptRoot "installers"
$postgresInstaller = (Get-ChildItem -Path $installersPath -Filter "postgresql-*.exe")[0]
$javaZip = (Get-ChildItem -Path $installersPath -Filter "openjdk-*.zip")[0]
$javaUnzipPath = Join-Path $PSScriptRoot "runtime/java"

# --- Installer Configuration ---
$installersPath = Join-Path $PSScriptRoot "installers"
$postgresInstaller = (Get-ChildItem -Path $installersPath -Filter "postgresql-*.exe")[0]
$javaZip = (Get-ChildItem -Path $installersPath -Filter "openjdk-*.zip")[0]
$javaUnzipPath = Join-Path $PSScriptRoot "runtime/java"

# ===================================================================
# HELPER FUNCTIONS
# ===================================================================

# Function to print a formatted header
function Write-Header {
    param($message)
    Write-Host "`n===================================================================" -ForegroundColor Cyan
    Write-Host $message -ForegroundColor Cyan
    Write-Host "===================================================================" -ForegroundColor Cyan
}

# Function to check if a command exists in the PATH
function Test-CommandExists {
    param($command)
    return (Get-Command $command -ErrorAction SilentlyContinue) -ne $null
}

# ===================================================================
# SCRIPT EXECUTION
# ===================================================================

# 1. VALIDATE PREREQUISITES
# ===================================================================
Write-Header "Step 1: Validating Prerequisites"

# Check for Administrator privileges
if (-Not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Error "Administrator privileges are required. Please re-run this script as an Administrator."
    exit 1
}
Write-Host "✔️ Running with Administrator privileges."

# Check for required files
if (-not $jarFile) { Write-Error "Application JAR not found in 'app' folder."; exit 1 }
if (-not $postgresInstaller) { Write-Error "PostgreSQL installer not found in 'installers' folder."; exit 1 }
if (-not $javaZip) { Write-Error "OpenJDK zip not found in 'installers' folder."; exit 1 }
Write-Host "✔️ All required deployment files are present."

# 2. SETUP JAVA RUNTIME
# ===================================================================
Write-Header "Step 2: Setting up Java Runtime"

if (Test-Path $javaUnzipPath) {
    Write-Host "Java runtime already exists at '$javaUnzipPath'."
} else {
    Write-Host "Unpacking Java runtime from '$($javaZip.Name)'..."
    Expand-Archive -Path $javaZip.FullName -DestinationPath $javaUnzipPath -Force
    Write-Host "✔️ Java runtime unpacked successfully."
}

# Find the actual Java home directory (it's usually inside one sub-folder)
$javaHome = (Get-ChildItem -Path $javaUnzipPath -Directory)[0].FullName
$javaBinPath = Join-Path $javaHome "bin"

# Add the bundled Java to the PATH for this session
$env:PATH = "$javaBinPath;$env:PATH"
$env:JAVA_HOME = $javaHome

if (-not (Test-CommandExists "java")) {
    Write-Error "Failed to add Java to the session PATH. Cannot continue."
    exit 1
}
Write-Host "✔️ Java is configured for this session."
java.exe -version

# 3. INSTALL AND CONFIGURE POSTGRESQL
# ===================================================================
Write-Header "Step 3: Setting up PostgreSQL Database"

# Add default PostgreSQL bin directory to path to check for 'psql'
$env:PATH = "C:\Program Files\PostgreSQL\14\bin;$env:PATH"

if (Test-CommandExists "psql") {
    Write-Host "✔️ PostgreSQL command 'psql' is already available. Skipping installation."
} else {
    Write-Host "PostgreSQL not found. Starting silent installation from '$($postgresInstaller.Name)'..."
    Write-Host "This may take several minutes. Please wait..."

    # Command-line arguments for a silent PostgreSQL installation
    $arguments = "--mode unattended --unattendedmodeui none --superpassword `"$dbPassword`" --serverport $dbPort"

    $process = Start-Process -FilePath $postgresInstaller.FullName -ArgumentList $arguments -Wait -PassThru

    if ($process.ExitCode -ne 0) {
        Write-Error "PostgreSQL installation failed with exit code $($process.ExitCode). Check installer logs."
        exit 1
    }
    Write-Host "✔️ PostgreSQL installed successfully."
}

# Check if the database exists
Write-Host "Checking for database '$dbName'..."
$env:PGPASSWORD = $dbPassword
$dbExists = psql -U $dbUser -h localhost -p $dbPort -lqt | findstr /I /C:"$dbName"
$env:PGPASSWORD = $null # Unset password

if ($dbExists) {
    Write-Host "✔️ Database '$dbName' already exists."
} else {
    Write-Host "Database '$dbName' not found. Creating it..."
    $env:PGPASSWORD = $dbPassword
    createdb -U $dbUser -h localhost -p $dbPort $dbName
    $env:PGPASSWORD = $null # Unset password
    Write-Host "✔️ Database '$dbName' created successfully."
}

# 4. DEPLOY THE SPRING BOOT APPLICATION
# ===================================================================
Write-Header "Step 4: Deploying the '$appName' Application"

# Stop any previously running instance of the application
Write-Host "Checking for existing running instances of '$appName'..."
try {
    $process = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like "*$($jarFile.Name)*" }
    if ($process) {
        Write-Host "Stopping existing process (PID: $($process.ProcessId))..."
        Stop-Process -Id $process.ProcessId -Force
        Write-Host "✔️ Process stopped."
    } else {
        Write-Host "✔️ No existing instance found."
    }
} catch {
    Write-Warning "Could not check for or stop existing processes. This might be a permissions issue."
}


# Start the application
Write-Host "Starting '$appName' application from '$($jarFile.FullName)'..."
$jarPath = $jarFile.FullName
Start-Process java -ArgumentList "-jar", "`"$jarPath`"" -NoNewWindow

# Wait a few seconds and check if the process started
Start-Sleep -Seconds 5
$newProcess = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like "*$($jarFile.Name)*" }
if ($newProcess) {
    Write-Host "✔️ Application '$appName' started successfully with PID: $($newProcess.ProcessId)." -ForegroundColor Green
} else {
    Write-Error "Application failed to start. Check the logs for more details."
    exit 1
}

Write-Header "Deployment Complete!"