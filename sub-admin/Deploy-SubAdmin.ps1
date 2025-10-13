# Deploy-SubAdmin.ps1

# ===================================================================
# CONFIGURATION
# ===================================================================
# --- Application Configuration ---
$appName = "sub-admin"
# Assumes the JAR is in a 'build/libs' sub-directory relative to the script
$jarFileName = "sub-admin-0.0.1-SNAPSHOT.jar" # <-- IMPORTANT: Update with your actual JAR file name
$jarPath = Join-Path $PSScriptRoot "build/libs" $jarFileName

# --- Database Configuration (from application.properties) ---
$dbName = "sub_admin_db"
$dbUser = "postgres"
$dbPassword = "postgres"
$dbPort = 5439 # As per your application.properties

# ===================================================================
# HELPER FUNCTIONS
# ===================================================================

# Function to check if a command exists
function Test-CommandExists {
    param($command)
    $exists = Get-Command $command -ErrorAction SilentlyContinue
    return $null -ne $exists
}

# Function to print a formatted header
function Write-Header {
    param($message)
    Write-Host "
===================================================================
$message
===================================================================" -ForegroundColor Cyan
}

# ===================================================================
# SCRIPT EXECUTION
# ===================================================================

# 1. CHECK PREREQUISITES
# ===================================================================
Write-Header "Step 1: Checking Prerequisites"

# Check for Chocolatey
if (-not (Test-CommandExists "choco")) {
    Write-Error "Chocolatey is not installed. Please install it first by running the following command in an Administrator PowerShell:"
    Write-Host "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
    exit 1
}
Write-Host "✔️ Chocolatey is installed."

# Check for Java
if (-not (Test-CommandExists "java")) {
    Write-Warning "Java is not found. Attempting to install OpenJDK..."
    choco install openjdk --version=17 -y
    # You might need to restart the shell for the new PATH to take effect.
    Write-Warning "Please restart your PowerShell terminal and re-run this script to ensure Java is available in the PATH."
    exit 1
}
Write-Host "✔️ Java is installed."

# 2. INSTALL AND CONFIGURE POSTGRESQL
# ===================================================================
Write-Header "Step 2: Setting up PostgreSQL Database"

# Check if PostgreSQL is installed
if (-not (Test-CommandExists "psql")) {
    Write-Host "PostgreSQL not found. Installing with Chocolatey..."
    # Installs PostgreSQL and sets the password for the 'postgres' user
    choco install postgresql14 --params "'/Password:$dbPassword'" -y --port $dbPort

    if (-not (Test-CommandExists "psql")) {
        Write-Error "PostgreSQL installation failed. Please check the Chocolatey logs."
        # Add PostgreSQL bin to PATH for the current session if choco didn't
        $env:Path += ";C:\Program Files\PostgreSQL\14\bin"
        Write-Host "Temporarily added PostgreSQL to PATH. Please re-run the script."
        exit 1
    }
}
Write-Host "✔️ PostgreSQL is installed."

# Check if the database exists
Write-Host "Checking for database '$dbName'..."
$env:PGPASSWORD = $dbPassword
$dbExists = psql -U $dbUser -h localhost -p $dbPort -lqt | cut -d \| -f 1 | grep -w $dbName
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


# 3. DEPLOY THE SPRING BOOT APPLICATION
# ===================================================================
Write-Header "Step 3: Deploying the '$appName' Application"

# Check if the JAR file exists
if (-not (Test-Path $jarPath)) {
    Write-Error "Application JAR not found at '$jarPath'. Please build the project first (e.g., './gradlew bootJar')."
    exit 1
}
Write-Host "✔️ Application JAR found at '$jarPath'."

# Stop any previously running instance of the application
# We find the process by the JAR file name in its command line arguments
Write-Host "Checking for existing running instances of '$appName'..."
try {
    $process = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like "*$jarFileName*" }
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
Write-Host "Starting '$appName' application..."
Start-Process java -ArgumentList "-jar", "`"$jarPath`"" -NoNewWindow

# Wait a few seconds and check if the process started
Start-Sleep -Seconds 5
$newProcess = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like "*$jarFileName*" }
if ($newProcess) {
    Write-Host "✔️ Application '$appName' started successfully with PID: $($newProcess.ProcessId)." -ForegroundColor Green
} else {
    Write-Error "Application failed to start. Check the logs for more details."
    exit 1
}

Write-Header "Deployment Complete!"