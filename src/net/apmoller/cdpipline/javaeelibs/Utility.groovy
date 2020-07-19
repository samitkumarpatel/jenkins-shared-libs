package net.samittutorial

import groovy.json.*

/**
 * This class is a wrapper on top of default Jenkins pipeline methods which are globally available to only Jenkinsfile
 * This approach will enhanced or restrict the default pipeline behaviour.
 * With this design pattern We can mock the Jenkins pipeline layer (which are already tested by Jenkins) and Only test the functionality we will be writing
 */

class Utility implements Serializable {
    def pipeline

    Utility(def pipeline) {
        this.pipeline=pipeline
    }

    def sh(_args) { pipeline.sh _args }
    def env() { pipeline.env }
    def docker() { pipeline.docker }
    def tool(_args) { pipeline.tool name:_args }
    def readFile(_filepath) { pipeline.readFile file:_filepath }
    def echo(_args) { pipeline.echo _args}
    def println(_args) { pipeline.println _args}
    def currentBuildVersion(_args) { pipeline.currentBuild.description=_args }
    def junit(_args) { pipeline.junit _args }
    def archiveArtifacts(artifacts,fingerprint) { pipeline.archiveArtifacts artifacts:artifacts, fingerprint: fingerprint }
    def fileExists(_path) { pipeline.fileExists _path }
    def readMavenPom(_path) { pipeline.readMavenPom file:_path}
    def writeFile(file,text) { pipeline.writeFile file: file, text: text }
    def customError(msg,code) {
        def confluence = "samittutorial.net"
        def errorMessage = """\
        ***********************************************************************************
        ERROR:
        ${msg}

        If you are unsure what this error means, please check the User Guide and FAQs below
        https://${confluence}/display/LCS/javaee+pipeline+faq

        Stack trace : ${code}
        ***********************************************************************************
        """.stripIndent()
        pipeline.println errorMessage
        throw code
    }

    def notifyStatus(notifyMethod="email"){
        if("email".equals(notifyMethod)){
            pipeline.println "email notification method selected"
        } else if("slack") {
            pipeline.println "slack notification method selected"
        } else {
            pipeline.println "default notification method selected"
        }

    }

    /**
     * This method is dependent on http_request Jenkins plugins
     * @param method
     * @param base64AuthToken
     * @param requestURL
     * @param payload
     * @return
     */
    def httpRestClient(base64AuthToken,requestURL, method="GET") {
        def response = pipeline.httpRequest url: requestURL,
                contentType:'APPLICATION_JSON',
                httpMode:method,
                customHeaders: [[name: 'Authorization', value: "Basic ${base64AuthToken}"]],
                ignoreSslErrors: true,
                httpProxy: ''
        def res= pipeline.readJSON text: response.content
        return res
    }

    /**
     * Pure Java based rest client. It does not require any Jenkins plugins
     * @param method
     * @param basicAuthBase64
     * @param requestURL
     * @param payload
     * @return
     */
    def httpRestClientV1(basicAuthBase64,requestURL,method="GET"){
        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn= (HttpURLConnection) url.openConnection()
            conn.setRequestMethod(method)
            conn.setRequestProperty( "Authorization", "Basic ${basicAuthBase64}")
            conn.connect()
            if(conn.getResponseCode()==200) {
                def jsonSlurper = new JsonSlurper()
                return jsonSlurper.parse(new InputStreamReader(conn.getInputStream(),"UTF-8"))
            } else {
                throw new Exception("Unexpected http status code ")
            }
        } catch(Exception e) {
            throw e
        }
    }

    def sendEmail(String rep_list) {
        pipeline.echo "Inside pipeline-common-libs shared library"
        pipeline.emailext subject: '$PROJECT_NAME - Build# $BUILD_NUMBER - $BUILD_STATUS!', body: '${SCRIPT, template="email.html"}', to: "$rep_list"
    }
}
