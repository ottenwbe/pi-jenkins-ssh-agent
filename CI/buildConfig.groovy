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

import java.util.regex.Pattern

def now 
def config
def gitCredentialsId 

def init() {
    now = new Date()
    config = readYaml file: 'CI/build-config.yaml'    
}

def configGit(gitUser, credentialsId) {
    gitCredentialsId = credentialsId
    withCredentials([sshUserPrivateKey(credentialsId: "${gitCredentialsId}", keyFileVariable: 'key', usernameVariable: 'USERNAME')]) {                
        sh "git config user.email '${USERNAME}'"
        sh "git config user.name '${gitUser}'"    
    }
}

def updateBaseImages() {
    ensureConfigured()
    updates = false

    updates = updates || tryUpdateBaseImage()
    updates = updates || tryUpdateForceBuild()

    writeConfig()
    updates
}

private def tryUpdateBaseImage() {
    imageTags = getBaseImageTags()
    if (baseImageNeedsUpdate(imageTags)) {
        updateBaseImage(imageTags[0])
        return true
    }
    return false
}

private def tryUpdateForceBuild() {
    if (config.forceBuild) {
        config.forceBuild = false
        return true
    }
    return false
}

private def getBaseImageTags() {
    def versionsResponse = sh(returnStdout: true, script: "curl -L -s '${config.baseImageURL}'").trim()                    
    def versions = readJSON text: versionsResponse
    def removals = []
    def pattern = Pattern.compile(config.pattern)
    versions.results.each { k -> if(!(k.name ==~ pattern)) { removals << k } }
    versions.results.removeAll(removals)
    versions.results
}

private def baseImageNeedsUpdate(tags) {
    ((tags.size() > 0) && (config.baseImageTag != tags[0].name))
}

private def updateBaseImage(tag) {
    config.baseImageTag = tag.name
    config.version.minor = config.version.minor + 1
}

private def updateForceBuild() {
    config.forceBuild = false
}

private def writeConfig() {
    sh "rm CI/build-config.yaml"
    writeYaml file: 'CI/build-config.yaml', data: config            
}

def publishToReleaseBranch() {
    ensureGitConfigured()
    sshagent(credentials: ["${gitCredentialsId}"]) {
        sh "git add CI/build-config.yaml"
        sh "git commit -m\"New Image Configured\""
        sh "git push origin HEAD:${config.releaseBranch}"                    
    }
}

private def ensureGitConfigured() {    
    if (!gitCredentialsId) {
        throw new Exception("Git was not configured. Call 'configGit()'!")
    }
}

private def ensureConfigured() {
    if (!config) {
        throw new Exception("Build was not configured. Call 'init()'!")
    }
}

def buildDate() {
    ensureConfigured()
    now.format("yyyy.MM.dd HH:mm:ss")                  
}

def imageTag() {
    ensureConfigured()
    "${version()}.${now.format("yyyyMMddHHmm")}"
}

def version() {
    ensureConfigured()
    "${config.version.major}.${config.version.minor}"    
}

def image() {
    ensureConfigured()
    "${config.dockerOrg}/${config.arch}-${config.app}"                     
}              

def releaseBranch() {
    ensureConfigured()
    config.releaseBranch
}

def baseImageTag() {
    ensureConfigured()
    config.baseImageTag
}

def maintainer() {
    ensureConfigured()
    config.maintainer
}

def forceBuild() {
    ensureConfigured()
    config.forceBuild
}

return this