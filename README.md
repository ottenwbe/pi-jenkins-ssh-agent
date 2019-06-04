# rpi-jenkins-ssh-agent
A Docker SSH Agent for Jenkins that runs on Raspberry Pis (ARM).

Based on
https://github.com/jenkinsci/docker-ssh-slave

## Build

### Build on Jenkins

1. Create a Pipeline or Multibranch Pipeline Project
1. Select this repository (git@github.com:ottenwbe/pi-jenkins-ssh-agent.git) or a fork
1. Select as Scriptpath 'Jenkinsfile'

### Build locally

```
docker build --build-arg RELEASE=stretch-20190524 -t rpi-jenkins-ssh-agent:test .
```
