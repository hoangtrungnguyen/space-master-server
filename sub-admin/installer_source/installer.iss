; /installer_source/installer.iss
; Inno Setup script for Sub Admin Application

#define AppName "Sub Admin"
#define AppVersion "0.0.1"
#define AppPublisher "Your Company Name"
#define AppURL "https://your-company.com"
#define AppExeName "sub-admin-service.exe"
#define PostgresInstaller "postgresql-14.9-1-windows-x64.exe"
#define PostgresPassword "postgres"
#define PostgresPort "5439"

[Setup]
; Basic application and installer info
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
AppPublisherURL={#AppURL}
AppSupportURL={#AppURL}
DefaultDirName={autopf}\{#AppName}
DisableProgramGroupPage=yes
PrivilegesRequired=admin ; Admin rights are needed to install services and software

; Output settings
OutputDir=userdocs:Output
OutputBaseFilename=SubAdmin-{#AppVersion}-Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
; 1. Copy the PostgreSQL installer to a temporary directory for execution
Source: "installers\{#PostgresInstaller}"; DestDir: "{tmp}"; Flags: deleteafterinstall

; 2. Copy the application JAR file
Source: "app\*.jar"; DestDir: "{app}\app"

; 3. Copy the service wrapper files
Source: "service\*"; DestDir: "{app}"

; NOTE: If you want to bundle Java, you would add it here.
; Example: Source: "java-runtime\*"; DestDir: "{app}\runtime"; Flags: recursesubdirs

[Icons]
; Optional: Create a Start Menu entry to uninstall
Name: "{group}\Uninstall {#AppName}"; Filename: "{uninstallexe}"

[Run]
; This section executes commands during the installation process.
; It runs AFTER files are copied.

; Step 1: Install PostgreSQL silently
; The --superpassword and --unattendedmodeui flags are crucial for a silent install.
Filename: "{tmp}\{#PostgresInstaller}"; \
    Parameters: "--mode unattended --unattendedmodeui none --superpassword ""{#PostgresPassword}"" --serverport {#PostgresPort}"; \
    StatusMsg: "Installing PostgreSQL database..."; \
    Flags: waituntilterminated

; Step 2: Install the application as a Windows Service using WinSW
Filename: "{app}\{#AppExeName}"; \
    Parameters: "install"; \
    StatusMsg: "Installing application service..."; \
    Flags: waituntilterminated runhidden

; Step 3: Start the newly installed service
Filename: "net"; \
    Parameters: "start {#AppName}"; \
    StatusMsg: "Starting application service..."; \
    Flags: waituntilterminated runhidden

[UninstallRun]
; This section runs when the user uninstalls the application.

; Step 1: Stop the service
Filename: "net"; \
    Parameters: "stop {#AppName}"; \
    Flags: waituntilterminated runhidden

; Step 2: Uninstall the service
Filename: "{app}\{#AppExeName}"; \
    Parameters: "uninstall"; \
    Flags: waituntilterminated runhidden