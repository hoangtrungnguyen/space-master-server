# Run Jar file

```shell
java -jar yourfile.jar
```
# Deploy

```shell
gsutil cp /Users/trungnguyenhoang/IdeaProjects/server/sub-admin/version.json gs://pos-vn-versioning/
```

# Deploy [Deploy-Offline.ps1](Deploy-Offline.ps1)

navigate to path
```shell
cd path\to\your\deploy
```
set permission
```shell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process
```

Execute script
```shell
.\Deploy-Offline.ps1
```


# 