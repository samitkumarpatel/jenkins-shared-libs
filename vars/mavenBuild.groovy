import net.apmoller.cdpipline.javaeelibs.Utility
import net.apmoller.cdpipline.javaeelibs.Maven
import net.apmoller.cdpipline.javaeelibs.Git
import net.apmoller.cdpipline.javaeelibs.SonarQube

def call(body={}) {
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    /**
     * Global variable needed during the build flow
     */
    def checkoutScm,buildVersion
    Utility wrapper = new Utility(this)
    Maven maven=new Maven(wrapper)
    SonarQube sonarqube=new SonarQube(wrapper, maven)
    Git git=new Git(this)
    boolean buildStatus = false
    /**
     * maven based build entry point
     */
    node(pipelineParams._slave ? null : pipelineParams._slave) {
        timestamps {
            try {
                properties([
                     buildDiscarder(logRotator(daysToKeepStr: '3', numToKeepStr: '10'))
                ])
                stage( 'Cleanup' ) {
                    //cleanWs deleteDirs: true, patterns: [[pattern: '*.m2*', type: 'EXCLUDE'],[pattern: '*.sonar*', type: 'EXCLUDE']]
                    deleteDir()
                }
                stage( 'Checkout' ) { checkoutScm = git.checkoutSCM() }
                stage( 'Compile & Test' ) {
                    maven.mvn("clean package")
                }
                def branchName=checkoutScm?.GIT_BRANCH?.split("origin/")[1]
                parallel 'sonarqube' : {
                    stage( 'Sonarqube Scan' ) {
                        withCredentials([string(credentialsId: 'mdev_sonarqube_token', variable: 'SONAR_TOKEN')]) {
                            sonarqube.sonarScan("${SONAR_TOKEN}")
                            sonarqube.sonarQualityGate("${SONAR_TOKEN}:".bytes.encodeBase64().toString())
                        }
                    }
                }, 'coverity' : {
                    stage( 'Coverity Scan' ) { println "Under Construction" }
                }, 'blackduck': {
                    stage( 'Blackduck Scan' ) { println "Under Construction" }
                }
                //TODO skip this for private branch build
                stage( 'Versioning' ) {
                    buildVersion = maven.mvnVersioning()
                    echo "$buildVersion"
                }
                stage( 'Nexus Deployment' ) { maven.mvn "deploy" }
                stage( 'Commit pom.xml' ) { git.commitChangedPOM(branchName,buildVersion) }
                stage( 'Git Tag' ) { git.tag(branchName,buildVersion) }
                buildStatus = true
            } catch(Exception exception) {
                buildStatus = false
                wrapper.customError(exception.getMessage(),exception)
            } finally {
                if(buildStatus) {
                    try {
                        junit 'target/surefire-reports/*.xml'
                        wrapper.notifyStatus("email")
                    } catch(Exception e) {
                        println "WARNIGN ${e.getMessage()}"
                    }

                }

            }
        }
    }
}
