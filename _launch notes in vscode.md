# launch notes in vscode

```
PowerShell (self)

$notes_name = "notes"

$dev_home = "c:/dev"
$tools_home = "${dev_home}/tools"

$notes_home = "${dev_home}/${notes_name}"
$vscode_home = "${tools_home}/vscode"
$git_home = "${tools_home}/git"

$env:Path = "${git_home}/bin;${vscode_home};" + $env:Path
```


# Launch vscode

```
cd $notes_home && code "${notes_name}.code-workspace" --profile "Notes" *> $null
```


# clone

```
cd $dev_home && git clone git@github.com:jamie-burns0/${notes_name}.git ${notes_name}
```
