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
    imageTags = getBaseImageTags()
    if (needsUpdate(imageTags)) {
        updateConfig(imageTags[0])
        writeConfig()
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

private def needsUpdate(tags) {
    (config.forceBuild) || ((tags.size() > 0) && (config.baseImageTag != tags[0].name))
}

private def updateConfig(tag) {
    config.baseImageTag = tag.name
    config.version.minor = config.version.minor + 1
    config.forceBuild = false
}

private def writeConfig() {
    sh "rm CI/build-config.yaml"
    writeYaml file: 'CI/build-config.yaml', data: config            
}

def publishToGit() {
    ensureGitConfigured()
    sshagent(credentials: ["${gitCredentialsId}"]) {
        sh "git add CI/build-config.yaml"
        sh "git commit -m\"Bump Base Image to ${config.baseImageTag}\""
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

return this