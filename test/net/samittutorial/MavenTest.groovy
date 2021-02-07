package net.samittutorial

import spock.lang.Specification

class MavenTest extends Specification {
    def utility

    def setup() {
        utility = Stub(Utility)

        //mocking jenkins builtin pipeline
        utility.env() >> [JENKINS_HOME: "/var/jenkins", EXECUTOR_NUMBER: 0, WORKSPACE: "/var/jenkins/workspace"]
        utility.sh(_) >> 0
        utility.docker() >> [image: { String _args -> [inside: {}]}]
        utility.junit(_) >> ""
        utility.archiveArtifacts(_,_) >> ""
    }

    def "mvn() with executionEnv=DOCKER"() {
        given:
        def maven = new Maven(utility)

        when:
        maven.mvn("clean")

        then:
        maven.executionEnv == ExecutionEnv.DOCKER
    }

    def "mvn() with executionEnv=JENKINS"() {
        given:
        def maven = new Maven(utility)
        maven.executionEnv = ExecutionEnv.JENKINS

        and:
        utility.tool(_) >> "/path/to/mvn"

        when:
        def r= maven.mvn("clean")

        then:
        maven.executionEnv == ExecutionEnv.JENKINS
        assert r == 0
    }

    def "mvn() with executionEnv=JENKINS with Exception "() {
        given:
        def maven = new Maven(utility)
        maven.executionEnv = ExecutionEnv.JENKINS

        and:
        utility.tool(_) >> { throw new Exception("No tool found")}

        when:
        def r= maven.mvn("clean")

        then:
        maven.executionEnv == ExecutionEnv.JENKINS
        thrown(Exception)
    }

    def "mvn() with executionEnv=OTHER "() {
        given:
        def maven = new Maven(utility)
        maven.executionEnv=ExecutionEnv.OTHER

        when:
        def r= maven.mvn("clean")

        then:
        maven.executionEnv == ExecutionEnv.OTHER
        r == 0
    }

    def "mvnVersioning test"() {
        given:
        def maven = new Maven(utility)

        and:
        utility.readMavenPom(_) >> [version: "1.0.0"]

        when:
        def version = maven.mvnVersioning("")

        then:
        version == "1.0.0"
    }

    def "mvnVersioning version not found scenario"() {
        given:
        def maven = new Maven(utility)

        and:
        utility.readMavenPom(_) >> [:]

        when:
        def version = maven.mvnVersioning("")

        then:
        version == "0.0.0"
    }

    def "placeMvnConfig Test"() {
        given:
        def maven = new Maven(utility)

        and:
        File f = File.createTempDir()
        utility.env() >> [JENKINS_HOME: "/target", WORKSPACE: f.path ]

        when:
        maven.placeMvnConfig()

        then:
        f.exists()
    }
}
