
# The Strategy
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



# folder
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