We will also use another small utility, WinSW (Windows Service Wrapper), to run your JAR file as a proper Windows service. This ensures it starts automatically on boot and can be managed like any other service.
Here’s the complete plan and all the code you'll need.
The Strategy
1.
Organize Files: We'll create a source directory containing your JAR, the PostgreSQL installer, and the WinSW utility.
2.
Configure WinSW: We'll create a small XML file to tell WinSW how to run your JAR.
3.
Write an Inno Setup Script: This script is the blueprint for the installer. It will define steps to:
◦
Copy the application files to the target machine (e.g., C:\Program Files\sub-admin).
◦
Run the PostgreSQL installer silently.
◦
Use WinSW to register and start your application as a Windows service.
4.
Compile: We'll use the Inno Setup compiler to package everything into a single setup.exe.
Step 1: Download Prerequisites
1.
Inno Setup: Download and install the compiler from jrsoftware.org.
2.
WinSW: Download the latest WinSW-x64.exe from the WinSW GitHub releases page.
3.
Your Files: Have your sub-admin-0.0.1-SNAPSHOT.jar and the postgresql-installer.exe ready.
Step 2: Organize Your Installer Source Files
Create a folder structure like this. This organization makes the installer script clean and easy to manage.
installer_source/
├── app/
│   └── sub-admin-0.0.1-SNAPSHOT.jar    # Your Spring Boot JAR
│
├── installers/
│   └── postgresql-14.9-1-windows-x64.exe # The PostgreSQL installer
│
├── service/
│   ├── sub-admin-service.exe           # This is the renamed WinSW-x64.exe
│   └── sub-admin-service.xml           # The WinSW configuration file (we will create this next)
│
└── installer.iss                         # The Inno Setup script (we will create this below)
Important: Rename WinSW-x64.exe to sub-admin-service.exe. WinSW reads its configuration from an XML file with the same base name.
Step 3: Create the WinSW Configuration File
This XML file tells your service wrapper (sub-admin-service.exe) what to do.
Create a new file named sub-admin-service.xml inside the installer_source/service/ folder.
Resource XML
<!-- /installer_source/service/sub-admin-service.xml -->
<service>
  <!-- A unique ID for the service -->
  <id>sub-admin</id>
  <!-- The name that will appear in the Windows Services list -->
  <name>Sub Admin Service</name>
  <!-- A description for the service -->
  <description>Backend service for the Sub Admin application.</description>

  <!--
    The executable to run. We are running Java.
    %BASE% is a WinSW variable that points to the directory where this XML file is.
    We assume a 'runtime' folder with Java will be placed here by the installer.
  -->
<executable>%BASE%\runtime\bin\java.exe</executable>

  <!-- The arguments to pass to the executable -->
<arguments>-Xmx256m -jar "%BASE%\app\sub-admin-0.0.1-SNAPSHOT.jar"</arguments>

  <!-- Set the working directory for the application -->
<workingdirectory>%BASE%\app</workingdirectory>

  <!-- Log settings -->
  <log mode="roll-by-size">
    <sizeThreshold>10240</sizeThreshold> <!-- 10 MB -->
    <keepFiles>8</keepFiles>
  </log>
</service>
Show full code block
Note: This configuration assumes you will also package a Java runtime. It's the most robust approach. If you want to rely on a system-installed Java, you can change <executable>java</executable>.
Step 4: Create the Inno Setup Script
This is the core of your installer. It defines every step of the installation process.
Create a new file named installer.iss in the root of your installer_source folder.
C
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
Show full code block
Step 5: Compile the Installer
1.
Open the Inno Setup Compiler application.
2.
Go to File -> Open... and select your installer.iss script.
3.
Go to Build -> Compile.
If everything is set up correctly, Inno Setup will process your script, package all the specified files, and produce a single SubAdmin-0.0.1-Setup.exe in the Output folder within your Documents.
This .exe file is now your complete, self-contained installer. You can distribute it to any Windows machine, and a simple double-click will install the database, set up your application as a service, and start it.