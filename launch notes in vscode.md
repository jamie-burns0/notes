# launch notes in vscode


PowerShell (self)
```
$dev_home = "c:/dev"
$tools_home = "c:/dev/tools"

$vscode_home = "${tools_home}/vscode"
$git_home = "${tools_home}/git"

$env:Path = "${git_home}/bin;${vscode_home};" + $env:Path
```


# Launch vscode

```
cd $dev_home/notes && code "notes.code-workspace" --profile "Notes"

cd $dev_home/private-notes && code "private-notes.code-workspace" --profile "Notes"
```


# clone

```
cd $dev_home && git clone git@github.com:jamie-burns0/notes.git notes

cd $dev_home && git clone git@github.com:jamie-burns0/private-notes.git private-notes
```
