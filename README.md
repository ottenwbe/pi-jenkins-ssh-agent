# pi-jenkins-ssh-agent
A Docker SSH Agent for Jenkins that runs on Raspberry Pis (ARM).

Based on:
https://github.com/jenkinsci/docker-ssh-slave

## Images

See: https://cloud.docker.com/repository/docker/ottenwbe/armv7-jenkins-ssh-agent

## Build

### Build locally

The ssh agent image can be built locally on an arm architecture, e.g., on a raspberry pi.
Note, the RPI_RELEASE build arg has to be set to specify the tag of the base image ([balenalib/rpi-raspbian](https://hub.docker.com/r/balenalib/rpi-raspbian)).

```
docker build --build-arg RPI_RELEASE=stretch-20190524 -t rpi-jenkins-ssh-agent:test .
```

### Build on Jenkins

A Jenkinsfile is included to run the as a [Jenkins](https://jenkins.io/) pipeline.

1. Ensure that the Docker plugin is installed and configured on Jenkins
1. Create a Pipeline or Multibranch Pipeline Project
    1. Select this [repository](https://github.com/ottenwbe/pi-jenkins-ssh-agent) or a fork of it as _Source_
    1. Select 'CI/Jenkinsfile' as _Scriptpath_
1. Configure the missing credentials in Jenkins
1. Trigger the pipeline to build the docker image
