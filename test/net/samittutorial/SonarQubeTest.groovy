package net.samittutorial

import groovy.json.JsonSlurper
import net.samittutorial.ExecutionEnv
import net.samittutorial.Maven
import net.samittutorial.SonarQube
import net.samittutorial.Utility
import spock.lang.Specification

class SonarQubeTest extends Specification {

    Utility wrapper = Stub() {
        sh(_) >> 0
        env() >> [JENKINS_HOME: "/var/jenkins/jenkins_home",WORKSPACE: "/var/jenkins/jenkins_home/workspace"]
        println(_) >> print("")
    }
    //This method is mocked , Just to evaluated the method call and parameter validation.
    Maven mvn = Mock()

    def restResponseAgainstCeTaskId = """
         {
             "task": {
                 "id": "AXA_cc9Bo9LaNp2OCLsP",
                 "type": "REPORT",
                 "componentId": "AW6ucHAmo9LaNp2OCER7",
                 "componentKey": "net.samittutorial:hello-world-ee",
                 "componentName": "hello-world-ee",
                 "componentQualifier": "TRK",
                 "analysisId": "AXA_cdbv-J62WRGsQWG-",
                 "status": "SUCCESS",
                 "submittedAt": "2020-02-13T16:46:28+0000",
                 "submitterLogin": "sonar_jenkins",
                 "startedAt": "2020-02-13T16:46:30+0000",
                 "executedAt": "2020-02-13T16:46:32+0000",
                 "executionTimeMs": 2293,
                 "logs": false,
                 "hasScannerContext": true,
                 "organization": "default-organization",
                 "warningCount": 0,
                 "warnings": []
             }
         }
         """.stripIndent()
    def restResponseAgainstCeTaskIdPENDING = """
         {
             "task": {
                 "id": "AXA_cc9Bo9LaNp2OCLsP",
                 "type": "REPORT",
                 "componentId": "AW6ucHAmo9LaNp2OCER7",
                 "componentKey": "net.samittutorial:hello-world-ee",
                 "componentName": "hello-world-ee",
                 "componentQualifier": "TRK",
                 "analysisId": "AXA_cdbv-J62WRGsQWG-",
                 "status": "PENDING",
                 "submittedAt": "2020-02-13T16:46:28+0000",
                 "submitterLogin": "sonar_jenkins",
                 "startedAt": "2020-02-13T16:46:30+0000",
                 "executedAt": "2020-02-13T16:46:32+0000",
                 "executionTimeMs": 2293,
                 "logs": false,
                 "hasScannerContext": true,
                 "organization": "default-organization",
                 "warningCount": 0,
                 "warnings": []
             }
         }
         """.stripIndent()
    def restResponseAgainstAnalysisId = """
         {
             "projectStatus": {
                 "status": "OK",
                 "conditions": [
                     {
                     "status": "OK",
                     "metricKey": "new_security_rating",
                     "comparator": "GT",
                     "periodIndex": 1,
                     "errorThreshold": "1",
                     "actualValue": "1"
                     },
                     {
                     "status": "OK",
                     "metricKey": "new_reliability_rating",
                     "comparator": "GT",
                     "periodIndex": 1,
                     "errorThreshold": "1",
                     "actualValue": "1"
                     },
                     {
                     "status": "OK",
                     "metricKey": "new_maintainability_rating",
                     "comparator": "GT",
                     "periodIndex": 1,
                     "errorThreshold": "1",
                     "actualValue": "1"
                     }
                 ],
                 "periods": [
                     {
                     "index": 1,
                     "mode": "previous_version",
                     "date": "2020-01-09T10:11:01+0000",
                     "parameter": "2.0.7"
                     }
                 ],
                 "ignoredConditions": false
             }
         }
         """.stripIndent()
    def restResponseAgainstAnalysisIdNOTOK = """
         {
             "projectStatus": {
                 "status": "NOTOK",
                 "conditions": [
                     {
                     "status": "OK",
                     "metricKey": "new_security_rating",
                     "comparator": "GT",
                     "periodIndex": 1,
                     "errorThreshold": "1",
                     "actualValue": "1"
                     },
                     {
                     "status": "NOTOK",
                     "metricKey": "new_reliability_rating",
                     "comparator": "GT",
                     "periodIndex": 1,
                     "errorThreshold": "1",
                     "actualValue": "1"
                     },
                     {
                     "status": "OK",
                     "metricKey": "new_maintainability_rating",
                     "comparator": "GT",
                     "periodIndex": 1,
                     "errorThreshold": "1",
                     "actualValue": "1"
                     }
                 ],
                 "periods": [
                     {
                     "index": 1,
                     "mode": "previous_version",
                     "date": "2020-01-09T10:11:01+0000",
                     "parameter": "2.0.7"
                     }
                 ],
                 "ignoredConditions": false
             }
         }
         """.stripIndent()

    def "sonarScan"() {
        given:
        def sonarQube = new SonarQube(wrapper, mvn)

        and:
        mvn.mvn(_) >> 0

        when:
        sonarQube.sonarScan('fake_credential')

        then:
        1 * mvn.mvn(_)
        notThrown(Exception)
    }

    def "sonarQualityGate With SUCCESS api response"() {
        given:
        def sonarQube = new SonarQube(wrapper, mvn)

        and:
        wrapper.fileExists(_) >> true
        wrapper.readFile(_) >> """
            projectKey=net.samittutorial:hello-world-ee
            serverUrl=https://localhost:9000
            serverVersion=7.5.0.20543
            dashboardUrl=https://localhost:9000/dashboard?id=net.samittutorial%3Ahello-world-ee
            ceTaskId=AXA_cc9Bo9LaNp2OCLsP
            ceTaskUrl=https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP
        """
        wrapper.httpRestClientV1(_, "https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP") >> new JsonSlurper().parseText(restResponseAgainstCeTaskId)
        wrapper.httpRestClientV1(_,"https://localhost:9000/api/qualitygates/project_status?analysisId=AXA_cdbv-J62WRGsQWG-") >> new JsonSlurper().parseText(restResponseAgainstAnalysisId)

        when:
        sonarQube.sonarQualityGate("",0)

        then:
        notThrown(Exception)
    }

    def "sonarQualityGate With PENDING api response"() {
        given:
        def sonarQube = new SonarQube(wrapper, mvn)

        and:
        wrapper.fileExists(_) >> true
        wrapper.readFile(_) >> """
            projectKey=net.samittutorial:hello-world-ee
            serverUrl=https://localhost:9000
            serverVersion=7.5.0.20543
            dashboardUrl=https://localhost:9000/dashboard?id=net.samittutorial%3Ahello-world-ee
            ceTaskId=AXA_cc9Bo9LaNp2OCLsP
            ceTaskUrl=https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP
        """
        wrapper.httpRestClientV1(_, "https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP") >> new JsonSlurper().parseText(restResponseAgainstCeTaskIdPENDING)
        wrapper.httpRestClientV1(_,"https://localhost:9000/api/qualitygates/project_status?analysisId=AXA_cdbv-J62WRGsQWG-") >> new JsonSlurper().parseText(restResponseAgainstAnalysisId)

        when:
        sonarQube.sonarQualityGate("",0)

        then:
        thrown(Exception)
    }

    def "sonarQualityGate - api response Exception scenario"() {
        given:
        def sonarQube = new SonarQube(wrapper, mvn)

        and:
        wrapper.fileExists(_) >> true
        wrapper.readFile(_) >> """
            projectKey=net.samittutorial:hello-world-ee
            serverUrl=https://localhost:9000
            serverVersion=7.5.0.20543
            dashboardUrl=https://localhost:9000/dashboard?id=net.samittutorial%3Ahello-world-ee
            ceTaskId=AXA_cc9Bo9LaNp2OCLsP
            ceTaskUrl=https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP
        """
        wrapper.httpRestClientV1(_, "https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP") >> new JsonSlurper().parseText(restResponseAgainstCeTaskId)
        wrapper.httpRestClientV1(_,"https://localhost:9000/api/qualitygates/project_status?analysisId=AXA_cdbv-J62WRGsQWG-") >> { throw new Exception("ouch") }

        when:
        sonarQube.sonarQualityGate("",0)

        then:
        thrown(Exception)
    }

    def "sonarQualityGate NOT OK scenario"() {
        given:
        def sonarQube = new SonarQube(wrapper, mvn)

        and:
        wrapper.fileExists(_) >> true
        wrapper.readFile(_) >> """
            projectKey=net.samittutorial:hello-world-ee
            serverUrl=https://localhost:9000
            serverVersion=7.5.0.20543
            dashboardUrl=https://localhost:9000/dashboard?id=net.samittutorial%3Ahello-world-ee
            ceTaskId=AXA_cc9Bo9LaNp2OCLsP
            ceTaskUrl=https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP
        """
        wrapper.httpRestClientV1(_, "https://localhost:9000/api/ce/task?id=AXA_cc9Bo9LaNp2OCLsP") >> new JsonSlurper().parseText(restResponseAgainstCeTaskId)
        wrapper.httpRestClientV1(_,"https://localhost:9000/api/qualitygates/project_status?analysisId=AXA_cdbv-J62WRGsQWG-") >> new JsonSlurper().parseText(restResponseAgainstAnalysisIdNOTOK)

        when:
        sonarQube.sonarQualityGate("",0)

        then:
        thrown(Exception)
    }
}
