# rpi-jenkins-ssh-agent
A Docker SSH Agent for Jenkins that runs on Raspberry Pis (ARM).

Based on
https://github.com/jenkinsci/docker-ssh-slave

## Build

```
docker build --build-arg RELEASE=stretch-20190524 -t rpi-jenkins-ssh-agent:test .
```
