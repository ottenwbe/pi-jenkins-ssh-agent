# pi-jenkins-ssh-agent
A docker ssh agent for Jenkins that runs on Raspberry Pis (ARM).
The ssh agent's docker image is based on Jenkin's [docker-ssh-slave](https://github.com/jenkinsci/docker-ssh-slave).

## Docker Images

[Images](https://hub.docker.com/r/ottenwbe/armv7-jenkins-ssh-agent) are hosted on [Docker Hub](https://hub.docker.com/) and can be used with the [Jenkins Docker Plugin](https://wiki.jenkins.io/display/JENKINS/Docker+Plugin).

```
https://hub.docker.com/r/ottenwbe/armv7-jenkins-ssh-agent
```

## Repo Structure

```
.
├── CI                      // Files required by the Jenkins pipeline 
│   ├── build-config.yaml   // Build configuration parameters 
│   ├── buildConfig.groovy  // Helper scripts 
│   ├── goss.yaml           // GOSS tests for the docker image
│   └── Jenkinsfile         // The pipeline
├── Dockerfile              // Docker ssh agent's (armv7) Dockerfile
├── LICENSE
├── README.md
└── setup-sshd              
```

## Build

### Build locally

The ssh agent's docker image can be built locally on an arm system, e.g., on a Raspberry Pi.
Note, the BASE_IMAGE_TAG build arg has to be set to specify the version of the underlying Raspbian ([balenalib/rpi-raspbian](https://hub.docker.com/r/balenalib/rpi-raspbian)).

```
git clone https://github.com/ottenwbe/pi-jenkins-ssh-agent.git
cd pi-jenkins-ssh-agent
docker build --build-arg BASE_IMAGE_TAG=stretch-20190524 -t rpi-jenkins-ssh-agent:test .
```

### Build with Jenkins

A Jenkinsfile is included to build the ssh agent with a [Jenkins](https://jenkins.io/) pipeline.

#### Prerequisites

* [Jenkins](https://jenkins.io/) (up and running)
* Jenkins Plugins:
    * [Docker Plugin](https://wiki.jenkins.io/display/JENKINS/Docker+Plugin). 
    * [Pipeline Utility Steps](https://plugins.jenkins.io/pipeline-utility-steps)
* The SMTP server is configured to send EMails. (see, ```Manage Jenkins > Configure System > Extended E-mail Notification```)
* The docker plugin is configured to spin up an existing version of the docker ssh agent for all builds labeled with ```sshagent``` on an arm system (e.g., a raspberry pi). (see, ```Manage Jenkins > Configure System > Docker```)

#### Configure Jenkins pipeline

1. Create a ```Pipeline``` or ```Multibranch Pipeline``` project
    1. Select this [repository](https://github.com/ottenwbe/pi-jenkins-ssh-agent) or a fork of it under _SCM_
    1. Select 'CI/Jenkinsfile' as _Script Path_
1. Configure all required credentials (```'dockerhub```, ```'github-bot'```) in Jenkins
1. Trigger the pipeline to build the docker image

#### Configure Build

The build is configured through the ```build-config.yaml``` file. This allows the Jenkins pipeline to automatically update the configuration.

```
baseImageTag: // stores the latest base image tag that was used to build the ssh agent image (Note: maintained by the Jenkins pipeline)
baseImageURL: // reference to the tags of the base image; used to find newer base image tags
pattern: // a pattern that describes the desired base image tag, e.g., stretch-\d{8} to only use stretch images
version: // the ssh agent's docker image is tagged as <major_version>.<minor_version>.<date of the build>
  major: <major_version>
  minor: <minor_version> // automatically incremented once the base image tag is updated to a newer tag  
maintainer: // maintainer label for the docker image
DockerOrg: // the docker organization to which the built image is pushed
app: jenkins-ssh-agent // <arch>-<app>: name of the docker image (armv7-jenkins-ssh-agent) 
arch: armv7
os: linux
releaseBranch: // only publish images and changes from this branch
forceBuild: // will force a build although no newer base image is found
```