package net.samittutorial

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

class Jobs {

   def pipeline

    Jobs(def pipeline) {
        this.pipeline=pipeline
    }

    def createJob(jobName) {
        
        def configXMLTemplate = """
            <flow-definition plugin="workflow-job@2.26">
                <description></description>
                <keepDependencies>false</keepDependencies>
                <properties>
                <hudson.model.ParametersDefinitionProperty>
                    <parameterDefinitions>
                        <hudson.model.StringParameterDefinition>
                            <name>inventory_branch</name>
                            <description>Ansible Inventory Branch</description>
                            <defaultValue>master</defaultValue>
                            <trim>true</trim>
                        </hudson.model.StringParameterDefinition>
                        <hudson.model.StringParameterDefinition>
                            <name>deploy_tag</name>
                            <description>Deployable Tag</description>
                            <defaultValue>latest</defaultValue>
                            <trim>true</trim>
                        </hudson.model.StringParameterDefinition>
                        <hudson.model.StringParameterDefinition>
                            <name>deploy_env</name>
                            <description>Deployment Environment</description>
                            <defaultValue>dev</defaultValue>
                            <trim>false</trim>
                        </hudson.model.StringParameterDefinition>
                        <hudson.model.BooleanParameterDefinition>
                            <name>debug</name>
                            <description>Debug</description>
                            <defaultValue>false</defaultValue>
                        </hudson.model.BooleanParameterDefinition>
                    </parameterDefinitions>
                </hudson.model.ParametersDefinitionProperty>
                <org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty/>
                <com.sonyericsson.rebuild.RebuildSettings plugin="rebuild@1.29">
                    <autoRebuild>false</autoRebuild>
                    <rebuildDisabled>false</rebuildDisabled>
                </com.sonyericsson.rebuild.RebuildSettings>
                </properties>
                <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.58">
                <scm class="hudson.plugins.git.GitSCM" plugin="git@3.9.1">
                    <configVersion>2</configVersion>
                    <userRemoteConfigs>
                        <hudson.plugins.git.UserRemoteConfig>
                            <url>https://github.com/samitkumarpatel/ansible-demo.git</url>
                            <credentialsId>fake</credentialsId>
                        </hudson.plugins.git.UserRemoteConfig>
                    </userRemoteConfigs>
                    <branches>
                        <hudson.plugins.git.BranchSpec>
                            <name>\$inventory_branch</name>
                        </hudson.plugins.git.BranchSpec>
                    </branches>
                    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
                    <submoduleCfg class="list"/>
                    <extensions/>
                </scm>
                <scriptPath>Jenkinsfile</scriptPath>
                <lightweight>false</lightweight>
                </definition>
                <triggers/>
                <disabled>false</disabled>
            </flow-definition>
        """.stripIndent()
        
        if(!Jenkins.getInstance().getItem(jobName)) {
            def templatedSerializeXml = new XmlUtil().serialize(
                new XmlParser().parseText(configXMLTemplate)
                )
            
            def finalXml = new XmlSlurper().parseText(templatedSerializeXml)
            
            byte[] configBytes= new StreamingMarkupBuilder()
                    .bindNode(finalXml)
                    .toString()
                    .getBytes()
            Jenkins.getInstance().createProjectFromXML(jobName, new ByteArrayInputStream(configBytes))
            
            pipeline.println "Pipeline ${jobName} created!"
        } else {
            pipeline.println "Pipeline ${jobName} already exist!..Skipping the creation process "
        }
    }

    def triggerJob(jobName, parameter) {
        return pipeline.build(job: jobName, parameters: [
                  pipeline.string(name: 'inventory_branch', value: parameter.inventory_branch),
                  pipeline.string(name: 'deploy_tag', value: parameter.deploy_tag),
                  pipeline.string(name: 'deploy_env', value: parameter.deploy_env),
                  pipeline.booleanParam(name: 'debug', value: parameter.debug)
              ], propagate: false, wait: true, quietPeriod: 5)
    }

    def jobStatus(jobReference) {
        pipeline.println jobReference.getResult()
    }
}