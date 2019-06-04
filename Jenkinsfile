/*
* MIT License
* 
* Copyright (c) 2019 Beate Ottenwälder
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/

pipeline { 
    agent { node { label 'sshagent' } } 
    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
        timeout(time: 2, unit: 'HOURS')
    }
    environment {
        APP         = 'jenkins-ssh-agent'
        VERSION     = '0.1.0'
        ARCH        = 'armv7' 
        OS          = 'linux'
        BUILD_DATE   = sh(returnStdout: true, script: "date +'%y.%m.%d %H:%M:%S'")
        RELEASE     = 'stretch-20190524'
        ORG         = 'ottenwbe'
    }
    stages {
        stage('Build'){
            steps {
                sh "docker -H ${env.PI_REMOTE_HOST} build --build-arg RELEASE=${env.RELEASE} --label 'version=${env.VERSION}' --label 'build_date=${env.BUILD_DATE}'  --label 'maintaner=Beate Ottenwaelder <b.ottenwaelder@gmail.com>' -t ${env.ORG}/${env.ARCH}-${env.APP}:${env.VERSION} ."
            }
        }
        stage('Publish') {           
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh "echo ${PASSWORD} | docker -H ${env.PI_REMOTE_HOST} login --username ${USERNAME} --password-stdin"
                    sh "docker -H ${env.PI_REMOTE_HOST} push ${env.ORG}/${env.ARCH}-${env.APP}:${env.VERSION}"                
                }
            }
        }        
    }
}
