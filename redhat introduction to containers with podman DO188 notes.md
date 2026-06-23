
# redhat introduction to containers with podman DO188 notes

- https://www.redhat.com/en/topics/containers
- https://opencontainers.org/about/overview/

## Lifecycle of Applications in Red Hat OpenShift Container Platform

### Kubernetes Overview

- https://kubernetes.io/docs/home/

Kubernetes is an orchestration service that simplifies the deployment, management, and scaling of containerized applications. It manages complex pools of resources, such as CPU, RAM, storage, and networking. Kubernetes provides high uptime and fault tolerance for containerized application deployments, removing the concern that developers might have regarding how their applications use resources.

The smallest manageable unit in Kubernetes is a pod, which represents a single application and consists of one or more containers, including storage resources and an IP address.

### Red Hat OpenShift Container Platform Overview

- https://docs.redhat.com/en/documentation/openshift_container_platform/4.18

Red Hat OpenShift Container Platform (RHOCP) is a set of modular components and services that are built on top of the Kubernetes container infrastructure. RHOCP adds capabilities to a production platform, such as remote management, multitenancy, increased security, monitoring and auditing, application lifecycle management, and self-service interfaces for developers.

- Starts with the definition of a pod and the containers that it is composed of, which contain the application.
- Pods are assigned to a healthy node.
- Pods run until their containers exit.
- Pods and their containers are removed from the node.

## podman

- https://podman.io/
- https://docs.podman.io/en/stable/
- https://podman-desktop.io/docs/intro

```
podman run --rm -d --name some-container-name \
    -p host-port:container-port \
    --net some-network,some-other-network
    -e SOMEVAR='some value' \
    path-to-image:version \
    command

podman -v

podman pull image-path:version

podman images

podman ps --all --format=json
```

### podman network

- https://github.com/podman-container-tools/podman/blob/main/docs/tutorials/basic_networking.md

DNS hostname of a container is its container name. So for ```--name some-name```, the host name will be ```some-name```

```
podman network create some-network

podman run ... --net some-network ...

podman inspect my-app \
  -f '{{.NetworkSettings.Networks.apps.IPAddress}}'
```

### port forwarding

Without a host specified, the container is assigned the broadcast address (0.0.0.0). This means that the container is accessible from all networks on the host machine. To publish a container to a specific host and to limit the networks it is accessible from, use the following form, 

```
podman run -p 127.0.0.1:host-port:container-port my-app
```

```
podman port --all
```

## container layers
```
ephemeral storage layer (RW)
image layer n (RO)
...
image layer 1 (RO)
image layer 0 (RO)
```

## start processes in containers

### Execute command(s) in a running container
```
podman exec [options] container-name [command...]
```

### open an iteractive session in a running container
```
podman exec -it container-name /bin/bash
```

## copy files in and out of containers
```
podman cp [options] [container:]source/path [container:]/destination/path

podman cp my-container:/tmp/logs ~/logs
podman cp nginx.conf ngnix-container:/etc/nginx
podman cp nginx-test:/etc/nginx/nginx.conf nginx-actual:/etc/nginx
```
# remote container development with visual studio code and podman
- https://developers.redhat.com/articles/2023/02/14/remote-container-development-vs-code-and-podman#



Building an image
- https://docs.podman.io/en/latest/markdown/podman-build.1.html
- https://github.com/containers/common/blob/main/docs/Containerfile.5.md


# POSTGRESQL
- https://github.com/docker-library/docs/blob/master/postgres/README.md
- https://hub.docker.com/_/postgres
- https://www.postgresql.org/docs/current/


mkdir /home/core/postgres-data

podman ... -v /home/core/postgres-data:/var/lib/postgresql/data:z


export IMAGE_NAME=postgres:bookworm
export CONTAINER_NAME=postgres_one
export DATA_PATH=/home/core/postgres-data
export NETWORK_NAME=podman
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres

# NOTE the ":Z" on the volume mapping. See,
- https://web.archive.org/web/20190728100417/https://www.projectatomic.io/blog/2015/06/using-volumes-with-docker-can-cause-problems-with-selinux/
- https://stackoverflow.com/questions/24288616/permission-denied-on-accessing-host-directory-in-docker
- https://github.com/docker-library/postgres/issues/116

podman run --name ${CONTAINER_NAME} --network ${NETWORK_NAME} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} -e POSTGRES_USER=${POSTGRES_USER} -e PGDATA=/var/lib/postgresql/data/pgdata -v ${DATA_PATH}:/var/lib/postgresql/data:Z  -d ${IMAGE_NAME}

podman logs ${CONTAINER_NAME}

export CONTAINER_IP=$(podman inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' ${CONTAINER_NAME})

podman run -it --rm --network ${NETWORK_NAME} ${IMAGE_NAME} psql -h ${CONTAINER_IP} -U ${POSTGRES_USER}


podman stop ${CONTAINER_NAME}
podman rm ${CONTAINER_NAME}


podman run -i --rm postgres:bookworm cat /usr/share/postgresql/postgresql.conf.sample | less

/^[a-zA-Z]