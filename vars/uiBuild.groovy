import net.samittutorial.Git

/**
 *
 * @param body
 * @return
 * This pipeline required below parameter from Jenkinsfile
  uiBuild() {
     projectname = "@xyz"
  }

 */
def call(body={}) {
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    pipelineParams.projectname == null ? "@mepc" :  pipelineParams.projectname
    node() {
        try {
            def _checkout, version, branchName,project
            Git git = new Git(this)
            
            stage('Clean') {
                cleanWs()
                println(pipelineParams)
            }
            stage('Checkout') {
                _checkout = git.checkoutSCM()
            }
            stage('npm config') {
                withCredentials([usernameColonPassword(credentialsId: 'mdev_nexus_token', variable: 'USERPASS')]) {
                    def base64Token = "${USERPASS}".bytes.encodeBase64().toString()
                    def npmrc = """
                        cache=${env.WORKSPACE}/npmcache
                        strict-ssl=false
                        always-auth=true
                        email=admin@samittutorial.net
                        registry=https://localhost:9001/repository/npm-group-internal
                        _auth=${base64Token}
                        scripts-prepend-node-path=true
                        HOME=${env.WORKSPACE}
                    """.stripIndent()
                    writeFile file:"${env.WORKSPACE}/.npmrc", text: npmrc
                    def bowerrc = """
                        {
                            "directory": "${env.WORKSPACE}/bower_components",
                            "allow_root": true
                        }
                    """
                    writeFile file:"${env.WORKSPACE}/.bowerrc", text: bowerrc
                }
            }
            docker.image("samitkumarpatel/nodejs-grunt-bower-alpine:7.4.0-alpine-01").inside("-w ${env.WORKSPACE}") {

                stage('sonarqube') {
                    println("//TODO")
                }
                stage('Versioning') {
                    println("versioning stratergy : patch")
                    sh "npm version patch"

                    def packagejson = readJSON file: "${env.WORKSPACE}/package.json"
                    version = packagejson?.version
                }
                stage('package') {
                    sh """
                        npm install 
                        bower install
                        ui_mepc_version=$version grunt -force release
                    """
                }
                stage('publish') {
                    sh """
                      npm publish $WORKSPACE/web --registry https://localhost:9001/repository/npm-hosted-internal/${pipelineParams.projectname}
                    """
                }
            }
            stage('tag/commit') {

                branchName = _checkout?.GIT_BRANCH?.split("origin/")[1]
                git.commitChanges(branchName,"package.json", "package.json change from pipeline - ${version}")
                git.tag(branchName,version)
            }
        } catch(Exception e) {
            throw e
        }
    }
}

