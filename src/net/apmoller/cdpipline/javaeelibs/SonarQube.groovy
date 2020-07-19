package net.apmoller.cdpipline.javaeelibs

class SonarQube implements Serializable {

    Utility pipeline
    Maven maven
    def sonarqubeUrl="https://sonar.maerskdev.net"
    def sonarqubeMavenPlugins="org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar"

    SonarQube(Utility pipeline,Maven maven) {
        this.pipeline=pipeline
        this.maven=maven
    }

    def sonarScan(sonarToken,_args="") {
        String sonarQubeJvmParameter="${sonarqubeMavenPlugins} -Duser.home=${pipeline.env().WORKSPACE} -Dsonar.host.url=${sonarqubeUrl} -Dsonar.login=${sonarToken} ${_args}"
        maven.mvn(sonarQubeJvmParameter)
    }

    private def validateCeTaskResponse(json) {
        assert json instanceof Map
        assert json?.task?.status: "No status was found in the response"
    }

    def sonarQualityGate(base64AuthToken,sleep=20000,_args="") {
        boolean doesFileExist=false
        def properties = new Properties()
        String path="${pipeline.env().WORKSPACE}".concat("/target/sonar/report-task.txt")
        try {
            for(int i=0;i<3;i++) {
                pipeline.echo "File Lookup count - ${i}"
                doesFileExist= pipeline.fileExists path
                if(doesFileExist){
                    break;
                }else{
                    continue;
                }
            }

            if(doesFileExist) {
                def fileData = pipeline.readFile path
                properties.load(new StringReader(fileData))
                pipeline.println properties
            } else {
                pipeline.echo "Expected file Not found"
                throw new RuntimeException("Expected file Not found")
            }

            def ceTaskResponse = pipeline.httpRestClientV1(base64AuthToken,properties?.ceTaskUrl?.toString())
            pipeline.println ceTaskResponse
            //assert to make sure response structure are good for sonarqube quality gate checks
            validateCeTaskResponse(ceTaskResponse)
            def qualityGateEvaluationStatus=false
            if(ceTaskResponse?.task?.status == "PENDING" || ceTaskResponse?.task?.status == "IN_PROGRESS" || ceTaskResponse?.task?.status =="CANCELED") {
                pipeline.echo "Sonarqube Quality Gate Evaluation Status : ${ceTaskResponse?.task?.status} - Retrying with input Count:3 , Interval: 20sec"
                for(int i=0;i<3;i++) {
                    pipeline.echo "Retry Count - ${i}"
                    def ceTaskRetryResponse = pipeline.httpRestClientV1(base64AuthToken, properties.ceTaskUrl.toString())
                    pipeline.println ceTaskRetryResponse
                    validateCeTaskResponse(ceTaskRetryResponse)
                    if( ceTaskRetryResponse?.task?.status == "SUCCESS") {
                        ceTaskResponse=ceTaskRetryResponse
                        qualityGateEvaluationStatus=true
                        break;
                    } else {
                        sleep(sleep)
                        continue;
                    }
                }
            } else {
                //TODO is not necessary. evaluate and remove
                qualityGateEvaluationStatus=true
            }

            if(qualityGateEvaluationStatus) {
                assert ceTaskResponse.task.status == "SUCCESS": "Scan resulted was successful"
                assert ceTaskResponse.task.analysisId: "No analysisId was found"

                def analysis_url = "${properties?.serverUrl?.toString()}/api/qualitygates/project_status?analysisId=${ceTaskResponse?.task?.analysisId}"

                pipeline.println("Analysis URL : ${analysis_url}")
                def projectStatusJson = pipeline.httpRestClientV1(base64AuthToken,analysis_url)

                assert projectStatusJson instanceof Map
                assert projectStatusJson?.projectStatus?.status: "No status was found"

                pipeline.println projectStatusJson
                pipeline.println "Checking Each Quality Gate"
                projectStatusJson?.projectStatus?.conditions.each { condition ->
                    if (condition.status == 'OK') {
                        pipeline.println "${condition.metricKey}: OK"
                    } else {
                        pipeline.echo "Qualitygate Fail"
                        throw new RuntimeException("Sonarqube Qualitygate Fail")
                    }
                }
            } else {
                throw new RuntimeException("Sonarqube analysis status is still : PENDING or IN_PROGRESS")
            }
            pipeline.println "Sonar scan completed successfully!"
        } catch(RuntimeException runtimeException) {
            throw runtimeException
        }
    }
}
