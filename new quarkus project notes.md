# new quarkus project notes

## new quarkus project

- https://docs.redhat.com/en/documentation/red_hat_build_of_quarkus/3.27/html/getting_started_with_red_hat_build_of_quarkus/assembly_quarkus-getting-started#proc_quarkus-creating-proj
- https://quarkus.io/version/3.27/guides/maven-tooling
- https://quarkus.io/version/3.27/guides/quarkus-maven-plugin

- https://code.quarkus.redhat.com

```
project_name=random-integers

quarkus_platform_version=3.27.0.redhat-00002

java_version=latest
maven_version=latest

projects_home=/home/jamie/dev/projects
project_home=$projects_home/$project_name

export JAVA_HOME=/opt/java/$java_version
export MAVEN_HOME=/opt/maven/$maven_version

export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH

cd $projects_home

projectGroupId=me.jamieburns
className=${projectGroupId}.MyResource
myPath="/hello"

mvn com.redhat.quarkus.platform:quarkus-maven-plugin:${quarkus_platform_version}:create \
    -DprojectGroupId=${projectGroupId} \
    -DprojectArtifactId=${project_name} \
    -DplatformGroupId=com.redhat.quarkus.platform \
    -DplatformVersion=${quarkus_platform_version} \
    -DclassName="${className}" \
    -Dpath="${myPath}"

rm -rf $projects_home/$project_name/mvnw* $projects_home/$project_name/.mvn/

cd $projects_home

tee -a ${projects_home}/${project_name}.code-workspace > /dev/null <<EOT
{
    "folders": [
        {
            "path": "${project_name}"
        },
        {
            "path": "${project_name}-devops"
        }
    ],
    "settings": {
        "files.autoSave": "afterDelay",
        "editor.inlayHints.enabled": "offUnlessPressed",
        "editor.parameterHints.enabled": false,
        "editor.quickSuggestions": {
            "other": "off",
            "comments": "off",
            "strings": "off"
        },
        "editor.quickSuggestionsDelay": 3000,
        "editor.inlineSuggest.showToolbar": "onHover",
        "editor.inlineSuggest.enabled": false,
        "editor.inlineSuggest.suppressSuggestions": true,
        "editor.detectIndentation": false,
        "editor.codeLens": false,
        "editor.folding": false,
        "editor.hover.delay": 3000,
        "editor.lightbulb.enabled": "onCode",
        "editor.minimap.enabled": true,
        "editor.minimap.size": "fit",
        "editor.snippetSuggestions": "none",
        "editor.suggest.preview": false,
        "java.signatureHelp.enabled": false,
        "java.autobuild.enabled": false,
        "java.project.explorer.showNonJavaResources": true,
        "java.dependency.packagePresentation": "flat",
        "java.configuration.updateBuildConfiguration": "disabled",
        "java.configuration.detectJdksAtStart": false,
        "java.compile.nullAnalysis.mode": "automatic",
        "maven.executable.preferMavenWrapper": false,
        "maven.terminal.useJavaHome": true,
        "workbench.startupEditor": "none"
    }
}
EOT

mkdir ${project_home}/.vscode && touch ${project_home}/.vscode/launch.json

tee -a ${project_home}/.vscode/launch.json > /dev/null <<EOT
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Attach to ${project_name}",
            "request": "attach",
            "hostName": "localhost",
            "port": "5005"
        }
    ]
}
EOT

```

We can use gh to create the remote repository from the command line
however we have to deal with auth tokens - what rights does our token need
then use "gh auth login" to login with our token
and then there will be a gh command to create the remote repository
FOR NOW, we create the repository on GitHub using the browser
then we continue the commands below

```
cd ${project_home}

git init -b main .
git remote add origin git@github.com:jamie-burns0/${project_name}.git

git status
git add .
git commit -m "create quarkus project"

git fetch --all --prune
git branch --set-upstream-to=origin/main main
git pull --rebase
git push
```