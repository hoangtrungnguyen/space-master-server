# This script builds the Spring Boot application and moves the resulting JAR
# to the specified deployment directory for the Inno Setup installer.
# It relies on Gradle Toolchains (defined in build.gradle.kts) to manage the JDK.

[CmdletBinding()]
param (
    # The destination directory for the built JAR file.
    [string]$TargetDir = "E:\Esupa\esupa-store-deployment\esupa_installer\backend\app\",
    [string]$UpdaterTargetDir = "E:\Esupa\esupa-store-deployment\esupa_updater\backend\app\"
)

# Stop the script if any command fails.
$ErrorActionPreference = 'Stop'

# --- 1. Build the Application ---
# We use 'clean' to remove previous build artifacts, then 'build' to create the bootable JAR.
# Gradle will use the Java 21 toolchain defined in build.gradle.kts.

Write-Host "Cleaning and starting Gradle build... (This will use the configured Java 21 toolchain)"
& ./gradlew clean build
Write-Host "✅ Build successful."

# --- 2. Locate the Built JAR ---
# This approach is robust and doesn't depend on a hardcoded version number.
$buildLibsDir = "$PSScriptRoot/build/libs"
$sourceJar = Get-ChildItem -Path $buildLibsDir -Filter "*.jar" | Where-Object { $_.Name -notlike "*-plain.jar" }

if (-not $sourceJar) {
    Write-Error "Build artifact (.jar) not found in $buildLibsDir. Aborting."
    exit 1
}

Write-Host "Found artifact: $($sourceJar.Name)"

# --- 3. Deploy the JAR to the Installer Target Location ---
Write-Host "[Installer] Deploying $($sourceJar.Name) to $TargetDir..."

# Create the target directory if it doesn't exist
if (-not (Test-Path -Path $TargetDir -PathType Container)) {
    Write-Host "Target directory not found. Creating $TargetDir..."
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

Copy-Item -Path $sourceJar.FullName -Destination $TargetDir -Force

Write-Host "✅ JAR file deployed successfully to $TargetDir."
Write-Host "---"


# --- 4. Deploy the Jar to Updater Target Location ---

Write-Host "[Updater] Deploying $($sourceJar.Name) to updater folder: $UpdaterTargetDir..."

# Create the target directory if it doesn't exist
if (-not (Test-Path -Path $UpdaterTargetDir -PathType Container)) {
    Write-Host "Target directory not found. Creating $UpdaterTargetDir..."
    New-Item -ItemType Directory -Force -Path $UpdaterTargetDir | Out-Null
}

Copy-Item -Path $sourceJar.FullName -Destination $UpdaterTargetDir -Force

Write-Host "✅ JAR file deployed successfully to updater folder: $UpdaterTargetDir."
Write-Host "---"
