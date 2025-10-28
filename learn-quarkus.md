```
wsl ~ -d kali-redhat-quarkus-wsl -e zsh
```

```
java_version=latest
maven_version=latest

projects_home=/home/jamie/dev/projects

export JAVA_HOME=/opt/java/$java_version
export MAVEN_HOME=/opt/maven/$maven_version

export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH

cd $projects_home
```

## launch project

```
project_name=learn-quarkus-01

java_version=latest
maven_version=latest

dev_home=/home/jamie/dev
projects_home=$dev_home/projects

export JAVA_HOME=/opt/java/$java_version
export MAVEN_HOME=/opt/maven/$maven_version

export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH

cd $projects_home

project_home=$projects_home/$project_name

cd $project_home && code ../$project_name.code-workspace
```

## new quarkus project

- https://docs.redhat.com/en/documentation/red_hat_build_of_quarkus/3.27/html/getting_started_with_red_hat_build_of_quarkus/assembly_quarkus-getting-started#proc_quarkus-creating-proj
- https://quarkus.io/version/3.27/guides/maven-tooling
- https://quarkus.io/version/3.27/guides/quarkus-maven-plugin

```
project_name=learn-quarkus-01

quarkus_platform_version=3.27.0.redhat-00002

cd $projects_home

mvn com.redhat.quarkus.platform:quarkus-maven-plugin:${quarkus_platform_version}:create \
    -DprojectGroupId=jamieburns.me \
    -DprojectArtifactId=${project_name} \
    -DplatformGroupId=com.redhat.quarkus.platform \
    -DplatformVersion=${quarkus_platform_version} \
    -DclassName="org.acme.quickstart.GreetingResource" \
    -Dpath="/hello"

rm -rf $projects_home/$project_name/mvnw* $projects_home/$project_name/.mvn/

cd $project_home

git init -b main .
git remote add origin git@github.com:jamie-burns0/learn-quarkus-01.git

git status
git add .
git commit -m "create quarkus project"

git fetch --all --prune
git branch --set-upstream-to=origin/main main
git pull --rebase

git push
```

```
tee -a ${projects_home}/${project_name}.code-workspace > /dev/null <<EOT
{
    "folders": [
        {
            "path": "${project_name}"
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
        "java.import.gradle.enabled": false,
        "java.import.gradle.wrapper.enabled": false,
        "java.import.gradle.annotationProcessing.enabled": false,
        "java.import.gradle.version": "8.9",
        "java.import.gradle.java.home": "/opt/java/jdk-22.0.2",
        "java.import.gradle.home": "/opt/gradle/gradle-8.9"
    }
}
EOT
```

## build kali-quarkus-wsl

```
$timestamp="20240801-1002"
$distro_name="kali-java-wsl"
$vm_name="kali-redhat-quarkus-wsl"

wsl --import $vm_name C:\dev\work\virtual-machines\$vm_name c:\dev\work\templates\$distro_name-base-$timestamp.tar

wsl ~ -d $vm_name -e zsh

sudo -E wget https://archive.kali.org/archive-keyring.gpg -O /usr/share/keyrings/kali-archive-keyring.gpg
sudo apt update && sudo apt full-upgrade && sudo apt autoremove

exit

wsl --shutdown

(wait 8 seconds)

wsl ~ -d $vm_name -e zsh
```

## GitHub CLI

```
sudo apt install gh
```

## Red Hat build of Quarkus (RBOQ)

### podman
```
sudo apt install podman
podman --version
```

### Red Hat OpenJDK

- https://docs.redhat.com/en/documentation/red_hat_build_of_quarkus/3.27/html/getting_started_with_red_hat_build_of_quarkus/assembly_quarkus-getting-started
- login to Red Hat Developer 
- jamie-burns0
- download Red Hat OpenJDK

```
sudo apt install xz-utils
xz --version

java_version=java-21-openjdk-21.0.9.0.10-1.portable.jdk.x86_64
java_download_filename=${java_version}.tar.xz

cp /mnt/c/Users/Jamie/Downloads/${JAVA_DOWNLOAD_FILENAME} /tmp
sudo mkdir -p /opt/java
sudo tar xvf /tmp/${java_download_filename} -C /opt/java
sudo ln -vfns /opt/java/${java_version} /opt/java/latest

rm /tmp/${java_download_filename}

/opt/java/latest/bin/java --version
```

### Maven

- https://maven.apache.org/download.cgi

```
export maven_version=apache-maven-3.9.11
export maven_download_filename=${maven_version}-bin.tar.gz

cp /mnt/c/users/jamie/downloads/${maven_download_filename} /tmp
sudo mkdir -p /opt/maven
sudo tar xvf /tmp/${maven_download_filename} -C /opt/maven
sudo ln -vfns /opt/maven/${maven_version} /opt/maven/latest

rm /tmp/${maven_download_filename}

JAVA_HOME=/opt/java/latest /opt/maven/latest/bin/mvn --version
```

### Use Red Hat maven repository

- original settings.xml can be found at MAVEN_HOME/conf/settings.xml

```
tee ~/.m2/settings.xml > /dev/null <<EOT
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  <mirrors>
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>external:http:*</mirrorOf>
      <name>Pseudo repository to mirror external repositories initially using HTTP.</name>
      <url>http://0.0.0.0/</url>
      <blocked>true</blocked>
    </mirror>
  </mirrors>

  <profiles>
    <!-- Configure the Red Hat build of Quarkus Maven repository -->
    <profile>
      <id>red-hat-enterprise-maven-repository</id>
      <repositories>
        <repository>
          <id>red-hat-enterprise-maven-repository</id>
          <url>https://maven.repository.redhat.com/ga/</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>red-hat-enterprise-maven-repository</id>
          <url>https://maven.repository.redhat.com/ga/</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>red-hat-enterprise-maven-repository</activeProfile>
  </activeProfiles>
</settings>
EOT
```

### Configuring Red Hat build of Quarkus extension registry client

- see https://docs.redhat.com/en/documentation/red_hat_build_of_quarkus/3.27/html/getting_started_with_red_hat_build_of_quarkus/assembly_quarkus-getting-started#proc_configuring-quarkus-extension-registry-client

```
mkdir -p $HOME/.quarkus && \
tee ~/.quarkus/config.yaml > /dev/null <<EOT
registries:
  - registry.quarkus.redhat.com
      offering: redhat
  - registry.quarkus.io
EOT
```


### Quarkus CLI

- https://quarkus.io/version/3.27/guides/cli-tooling

```
export JAVA_HOME=/opt/java/latest

curl -Ls https://sh.jbang.dev | zsh -s - trust add https://repo1.maven.org/maven2/io/quarkus/quarkus-cli/
curl -Ls https://sh.jbang.dev | zsh -s - app install --fresh --force quarkus@quarkusio

exit

wsl ~ -d kali-redhat-quarkus-wsl -e zsh

export JAVA_HOME=/opt/java/latest
quarkus --version
```
