# MIT License
# 
# Copyright (c) 2019 Beate Ottenwälder
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

ARG RPI_RELEASE
FROM balenalib/rpi-raspbian:${RPI_RELEASE}

ARG user=jenkins
ARG group=jenkins
ARG uid=1000
ARG gid=1000
ARG dockergroup=docker
ARG dockergid=996
ARG JENKINS_AGENT_HOME=/home/${user}

ENV JENKINS_AGENT_HOME ${JENKINS_AGENT_HOME}

RUN groupadd -g ${dockergid} ${dockergroup} \
    && groupadd -g ${gid} ${group} \
    && useradd -d "${JENKINS_AGENT_HOME}" -u "${uid}" -g "${gid}" -m -s /bin/bash "${user}"  \
    && usermod -aG ${dockergroup} "${user}"    

COPY setup-sshd /usr/local/bin/setup-sshd
RUN chmod u+rx /usr/local/bin/setup-sshd

# update the system, install dependencies, git, java, and then docker
RUN apt-get update \
    && apt-get -y --no-install-recommends install \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg-agent  \
        software-properties-common \
        git \
        openjdk-8-jdk \
        openssh-server \
    && curl -fsSL https://download.docker.com/linux/raspbian/gpg | sudo apt-key add - \
    && echo "deb [arch=armhf] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get -y --no-install-recommends install docker-ce \    
    && apt-get clean && apt-get autoremove -q \
    && rm -rf /var/lib/apt/lists/* /usr/share/doc /usr/share/man /tmp/*

RUN sed -i /etc/ssh/sshd_config \
        -e 's/#PermitRootLogin.*/PermitRootLogin no/' \
        -e 's/#RSAAuthentication.*/RSAAuthentication yes/'  \
        -e 's/#PasswordAuthentication.*/PasswordAuthentication no/' \
        -e 's/#SyslogFacility.*/SyslogFacility AUTH/' \
        -e 's/#LogLevel.*/LogLevel INFO/' \
    && mkdir /var/run/sshd

RUN curl -fsSL https://goss.rocks/install -o install-goss.sh \
    && sed -i 's/arch="386"/arch="arm"/g' install-goss.sh \
    && chmod 755 install-goss.sh \
    && ./install-goss.sh \
    && rm install-goss.sh

VOLUME "${JENKINS_AGENT_HOME}" "/tmp" "/run" "/var/run"
WORKDIR "${JENKINS_AGENT_HOME}"

EXPOSE 22

ENTRYPOINT ["/usr/local/bin/setup-sshd"]