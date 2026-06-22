
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
podman run --rm -d --name somename \
    -p host-port:container-port \
    -e SOMEVAR='some value' \
    path-to-image:version \
    command

podman -v

podman pull image-path:version

podman images

podman ps --all --format=json
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