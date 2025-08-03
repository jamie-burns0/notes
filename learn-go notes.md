learn-go notes

# launch in WSL

```
project_name=learn-go

go_version=go1.23.1

projects_home=/home/jamie/dev/projects
project_home=$projects_home/go/learn-go

export GOROOT=/opt/go/$go_version

export PATH=$GOROOT/bin:$PATH

cd $project_home && code $project_name.code-workspace
```

# Launch in Windows

```
$project_name = "learn-go"

$go_version = "go-1.24.4"

$projects_home = "c:/dev/projects"
$tools_home = "c:/dev/tools"

$project_home = "${projects_home}/go/${project_name}"
$git_home = "${tools_home}/git"
$vscode_home = "${tools_home}/vscode"

$env:GOROOT = "${tools_home}/${go_version}"

$env:Path = "$env:GOROOT/bin;${git_home}/bin;${vscode_home};" + $env:Path

cd $project_home && code ${project_name}.code-workspace --profile "Go"
```


# checkout

```
cd $projects_home && git clone git@github.com:jamie-burns0/go.git go
```