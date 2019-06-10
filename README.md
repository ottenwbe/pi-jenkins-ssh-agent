# pi-jenkins-ssh-agent
A Docker ssh agent for Jenkins that runs on Raspberry Pis (ARM).
This docker image is based on Jenkin's [docker-ssh-slave](https://github.com/jenkinsci/docker-ssh-slave).

## Docker Images

[Images](https://hub.docker.com/r/ottenwbe/armv7-jenkins-ssh-agent) are hosted on [Docker Hub](https://hub.docker.com/) and can be used with the [Jenkins Docker Plugin](https://wiki.jenkins.io/display/JENKINS/Docker+Plugin).

```
https://hub.docker.com/r/ottenwbe/armv7-jenkins-ssh-agent
```

## Repo Structure

```
.
├── CI                      // Jenkins pipeline related files 
│   ├── build-config.yaml   // Build configuration parameters 
│   ├── buildConfig.groovy  // Helper scripts 
│   ├── goss.yaml           // GOSS tests for the Docker image
│   └── Jenkinsfile         // The pipeline
├── Dockerfile              // Docker ssh agent's (armv7) Dockerfile
├── LICENSE
├── README.md
└── setup-sshd              
```

## Build

### Build locally

The Docker ssh agent image can be built locally on an arm architecture, e.g., on a raspberry pi.
Note, the BASE_IMAGE_TAG build arg has to be set to specify the version of the underlying Raspbian ([balenalib/rpi-raspbian](https://hub.docker.com/r/balenalib/rpi-raspbian)).

```
git clone https://github.com/ottenwbe/pi-jenkins-ssh-agent.git
cd pi-jenkins-ssh-agent
docker build --build-arg BASE_IMAGE_TAG=stretch-20190524 -t rpi-jenkins-ssh-agent:test .
```

### Build with Jenkins

A Jenkinsfile is included to build the Docker ssh agent in a [Jenkins](https://jenkins.io/) pipeline.

#### Configure Jenkins

1. Ensure that the Docker plugin is installed and configured on Jenkins
    1. Configure the plugin to spin up a previous version of the Docker ssh agent for all builds labeled with ```sshagent```.
1. Create a ```Pipeline``` or ```Multibranch Pipeline``` project
    1. Select this [repository](https://github.com/ottenwbe/pi-jenkins-ssh-agent) or a fork of under _SCM_
    1. Select 'CI/Jenkinsfile' as _Script Path_
1. Configure the missing credentials in Jenkins
1. Trigger the pipeline to build the docker image

#### Configure Build