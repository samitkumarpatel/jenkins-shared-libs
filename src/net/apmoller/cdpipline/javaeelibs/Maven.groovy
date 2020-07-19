package net.apmoller.cdpipline.javaeelibs

/**
 * This is class is maven based wrapper , which does mvn related activity with all necessary mvn args
 *
 * @author spa349
 */

class Maven implements Serializable {

    Utility pipeline
    ExecutionEnv executionEnv = ExecutionEnv.DOCKER
    List mvnArgs
    def mavenDockerImage='docker.maerskdev.net/maven:3-alpine'
    def mavenToolName="maven_3_6_2"
    def mavenPath="mvn"

    Maven(Utility pipeline) {
        this.pipeline=pipeline
        mvnArgs = new ArrayList()
    }
    /**
     * This method will decide and evaluate mvn command path based on necessary class level parameter.
     * @param _args
     * @return
     */
    def mvn(_args="") {
        try {
            if(!mvnArgs) {
                mvnArgs << "--batch-mode"
                mvnArgs << "-s ${pipeline.env().WORKSPACE}@tmp/m2.xml -Dsettings.security=${pipeline.env().WORKSPACE}@tmp/m2-meta.xml"
              	mvnArgs << "-Dmaven.repo.local=${pipeline.env().WORKSPACE}/.m2"
            }
            placeMvnConfig()
            if(executionEnv == ExecutionEnv.DOCKER) {
                pipeline.docker().image(mavenDockerImage).inside() {
                    pipeline.sh "${mavenPath} ${mvnArgs.join(" ")} ${_args}"
                }
            } else if(executionEnv == ExecutionEnv.JENKINS) {
                def mvn="${pipeline.tool(mavenToolName)}/bin/mvn"
                pipeline.sh "$mvn ${mvnArgs.join(" ")} ${_args}"
            } else {
                pipeline.sh "${mavenPath} ${mvnArgs.join(" ")} ${_args}"
            }
        } catch(Exception e) {
            throw e
        }
    }

    def mvnVersioning(_args="") {
        def command='build-helper:parse-version versions:set -DnewVersion=\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.nextIncrementalVersion} versions:commit'
        mvn(command)
        def pom = pipeline.readMavenPom("${pipeline.env().WORKSPACE}/pom.xml")
        def buildVersion = pom.version ? pom.version : '0.0.0'
        if(buildVersion) {
            pipeline.echo("Current Evaluated Build Version :${buildVersion}")
            pipeline.currentBuildVersion("$buildVersion")
            return buildVersion
        }
    }

    private def placeMvnConfig() {
        def securitySettingsDotXml = """
            <settingsSecurity>
              <master>{sjc9N6R1cKQHa2cCLnuoSZPZ6ajpR+nUDHdJAsMqQsM=}</master>
            </settingsSecurity>
        """.stripIndent()

        def settingsDotXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
                <mirrors>
                    <mirror>
                        <id>devops-nexus</id>
                        <mirrorOf>*</mirrorOf>
                        <name>Main mirror for all devops managed repositories</name>
                        <url>https://tools-nexus.maerskdev.net/repository/maven-group-public/</url>
                    </mirror>
                </mirrors>
                <servers>
                    <server>
                        <id>net.apmoller.crb.devops.snapshots</id>
                        <username>nexuswriter</username>
                        <password>{pXUMJvvm8SQHgSUHwK4irmd4bKEIsaaN4GYBAKzU//CwBj0sRNjCPuGZkO+sqSfz}</password>
                    </server>
                    <server>
                        <id>net.apmoller.crb.devops.releases</id>
                        <username>nexuswriter</username>
                        <password>{pXUMJvvm8SQHgSUHwK4irmd4bKEIsaaN4GYBAKzU//CwBj0sRNjCPuGZkO+sqSfz}</password>
                    </server>
                    <server>
                        <id>devops-nexus</id>
                        <username>nexuswriter</username>
                        <password>{pXUMJvvm8SQHgSUHwK4irmd4bKEIsaaN4GYBAKzU//CwBj0sRNjCPuGZkO+sqSfz}</password>
                    </server>
                    <server>
                        <id>net.maerskdev.cdpipeline.releases</id>
                        <username>nexuswriter</username>
                        <password>{pXUMJvvm8SQHgSUHwK4irmd4bKEIsaaN4GYBAKzU//CwBj0sRNjCPuGZkO+sqSfz}</password>
                    </server>
                    <server>
                        <id>net.maerskdev.cdpipeline.snapshots</id>
                        <username>nexuswriter</username>
                        <password>{pXUMJvvm8SQHgSUHwK4irmd4bKEIsaaN4GYBAKzU//CwBj0sRNjCPuGZkO+sqSfz}</password>
                    </server>
                </servers>
            </settings>
        """.stripIndent()
        pipeline.writeFile("${pipeline.env().WORKSPACE}@tmp/m2-meta.xml",securitySettingsDotXml)
        pipeline.writeFile("${pipeline.env().WORKSPACE}@tmp/m2.xml",settingsDotXml)

    }
}
