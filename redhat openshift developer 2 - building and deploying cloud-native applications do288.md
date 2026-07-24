# redhat openshift developer 2 - building and deploying cloud-native applications DO288

```
uptime

ssh lab@utility
./wait.sh
```

![high-level functional overview](./redhat-openshift-developer-2-building-and-deploying-cloud-native-applications-do288/redhat-openshift-high-level-functional-overview.png)

![architecture of red hat openshift](./redhat-openshift-developer-2-building-and-deploying-cloud-native-applications-do288/architecture-of-red-hat-openshift.png)

## kubernetes concepts

#### Pods
A Pod is a collection of containers that share the same storage and network. Pods share the context by using Linux namespaces, cgroups, and other isolation technologies.

Each container in a pod usually contains applications that are more or less logically coupled.

#### ReplicaSet
The ReplicaSet object indicates the number of pods that are available to attend a request. This object also ties all the pods replicas together so you can operate on them at the same time.

#### Deployments
A Deployment contains the desired state of an application's pods and uses a ReplicaSet to achieve this desired state. Some changes in the application's state can be: creating pods, declaring a new state of pods, changing the number of pods, or rolling back to a previous Deployment revision.

#### Service
A Kubernetes Service exposes a set of pods over a network. This abstraction allows internal or external clients of the application running on said pods to connect to them regardless of the actual state of the replicas or varying network IPs.

#### Ingress
An Ingress exposes services inside the cluster to outside clients by using HTTP or HTTPS. A service ingress can also provide external URLs, load balancing, name-based virtual hosting, or SSL/TLS termination.

#### Namespace
A Namespace can enable you to isolate resources, encapsulate objects under a unique name, and provide resource quotas.

#### Custom Resource
Custom Resource (CR) allows extending the Kubernetes API. Custom resources represent entities other than the default ones in Kubernetes. Additionally, Custom resources interact with other cluster objects, regardless of whether those other objects are default or custom.

#### Operator
An Operator is a custom Kubernetes controller that uses custom resources to deploy and manage applications. It takes high-level user configuration and acts to make the cluster match the desired state.

#### Service Account
A Service Account is a special kind of account that does not correspond to an actual user, but it is used internally by cluster tools. It is useful for pods to connect to objects in the cluster, such as CI/CD pipelines, secrets, or external resources (outside of the namespace or the cluster).

#### Storage Class
A Storage Class is a name that identifies a particular kind of storage defined by the cluster administrator. A storage class also defines its characteristics, such as backup policies, service level quality, or any other specification the administrator might choose.

#### Persistent Volume
A Persistent Volume (PV), is a persistence storage unit offered by the cluster, independent of cluster nodes. This object holds information regarding the size, type, or ability to share storage.

#### Persistent Volume Claim
Users claim the storage that a PV offers by using a Persistent Volume Claim (PVC). A PVC is a request to access a specific kind of storage of the required size. After acquiring the PVC, the storage is attached to the pods claiming it.

## Red Hat OpenShift Extends Kubernetes
In the following list, you can find how Red Hat OpenShift extends the basic functionality of Kubernetes:

#### Build
A Build is the generic process of taking an input source, such as the source code of an application, and producing a usable resource as the output, such as a runnable image.

#### BuildConfig
An object that defines a build process. A build object requires, at least, the source input of the build, the build strategy that defines how to build the input, and where to store the output of the process.

#### Route
A Route serves a similar purpose to the Kubernetes ingress but provides additional features: TLS passthrough and re-encryption, wildcard domains, pattern-based domains, and splitting traffic.

#### Project
A Project extends Kubernetes namespaces by adding templated project creation and finer control over user permissions.

#### Internal DNS
Red Hat OpenShift provides an internal DNS server that adds automatic DNS service resolution for services in the same project.

#### Security Context Constraints
In addition to hardening and providing finer role-based user control, Red Hat OpenShift provides Security Context Constraints (SCC), which control pod permissions.

## Deploying Applications with oc new-app

The oc new-app command provides the following options to customize the application build:

| Option	| Description |
|:-|:-|
|--image-stream, -i | The image stream to be used to deploy a container image |
|-e| environment variable KEY=value |
|-l| label key=value |
|--name | provide a different default name (label is app=value)|
|-o | use ```-o yaml``` to inspect resource definitions without creating resources |
|--strategy | Manually specifies the containerization strategy, such as docker, or source |
|--code | The URL to a Git repository to be used as input for an S2I build |
|--image |	The URL to a container image to be deployed |
|--dry-run | Set to true to show the result of the operation without performing it |
|--context-dir | The path to a directory inside of the git repository to be treated as the application root |

```
oc login -u developer -p developer https://api.ocp4.example.com:6443
oc project deploy-cli
oc new-app --name=weather registry.ocp4.example.com:8443/redhattraining/openshift-dev-deploy-cli-weather:1.0
oc get all
oc expose --name=weather service/openshift-dev-deploy-cli-weather
oc get route weather
curl weather-deploy-cli.apps.ocp4.example.com/tomorrow
oc delete all -l app=weather
```

## odo openshift client

- https://odo.dev/docs/introduction/

> __*Note*__: odo is for developers

odo is different as it focuses on application developers and cloud engineers. Both kubectl and oc are DevOps oriented tools and help in deploying applications to and maintaining a Kubernetes cluster provided you know Kubernetes well.

The odo CLI gives a Kubernetes-native developer experience, enabling you to build, run, and debug your application directly on the cluster. These operations are all considered __*inner loop*__ development. The inner loop is an iterative process that developers run locally before they commit source code changes.

With odo, you can define the process that your application must follow to go from source code to the components running in a production cluster. The set of tasks that compose this process is also known as the __*outer loop*__.

- see https://odo.dev/docs/introduction/#what-is-inner-loop-and-outer-loop

![inner loop vs outer loop](./redhat-openshift-developer-2-building-and-deploying-cloud-native-applications-do288/inner-loop-vs-outer-loop.png)

You can implement your deployment process within the odo outer loop, such as the following:

- Build the deployment artifact
- Run integration tests
- Run security tests
- Deploy the application

Developers can trigger the execution of the preceding steps with odo for every application change. The instructions that odo must follow for the inner loop and the outer loop are defined in a devfile.yaml file.

The devfile is composed of components, which are Kubernetes-related entities such as container images, containers, manifest files, and volumes. The devfile contains commands that reference components and tie them to an execution phase. Command groups within a devfile are the execution phases.

The odo init command creates a bootstrap devfile and tries to determine an appropriate runtime based on source code in the current working directory. If you do not specify additional options to the odo init command, then odo starts an interactive shell session for configuring the devfile. This command requires a connection to a devfile registry such as https://registry.devfile.io, which is the default.

The current devfile specification defines the following command groups:

- build
- run
- test
- debug
- deploy

The odo command offers administrative features to work with the cluster.

### Defining Outer Loop Tasks With odo

```
schemaVersion: 2.2.0
metadata:
  name: nodejs
  version: 2.1.1
  displayName: Node.js Runtime
  description: Stack with Node.js 16
  tags: ["Node.js", "Express", "ubi8"]
  projectType: "Node.js"
  language: "JavaScript"
  provider: Red Hat
  supportUrl: https://github.com/devfile-samples/devfile-support#support-information
parent:
  id: nodejs
  registryUrl: "https://registry.devfile.io"
components:
  - name: image-build
    image:
      imageName: nodejs-image:latest
      dockerfile:
        uri: Dockerfile
        buildContext: .
        rootRequired: false
  - name: kubernetes-deploy
    kubernetes:
      uri: deploy.yaml
commands:
  - id: build-image
    apply:
      component: image-build
  - id: deployk8s
    apply:
      component: kubernetes-deploy
  - id: deploy
    composite:
      commands:
        - build-image
        - deployk8s
      group:
        kind: deploy
        isDefault: true
```

```
odo login -u developer -p developer https://api.ocp4.example.com:6443
odo create project odo-deploy-cli
oc project odo-deploy-cli
cd /path/to/devfile.yaml
odo deploy
oc get all
curl weather-odo-deploy-cli.apps.ocp4.example.com/tomorrow
```
### Container Image Renaming with odo

To provide devfile portability, odo can customize container image names to work with your container registry. This image renaming process works for the image names that exist in the devfile or the Kubernetes resources.

The image renaming logic only triggers if you define the ImageRegistry preference by running the following command:

``` [user@host ~]$ odo preference set ImageRegistry REGISTRY_URL/NAMESPACE```

To select an image name for renaming, you must create an image component with an imageName property that contains a relative image. A relative image name does not include the container registry.

```
...
components:
  - image:
      imageName: "my-relative-image"
    name: relative-image
...
```

The final image names have the following pattern:

```ImageRegistry/DevfileName-ImageName:UniqueId```

You can verify the results by inspecting the image registry where odo creates the images or by inspecting the Kubernetes sources that odo creates in the cluster.
