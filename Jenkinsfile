node() {
    docker.image("maven:3-alpine").inside {
        stage('checkout') { checkout scm }
        stage('Build Verify') {
            sh """
                mvn -B -e clean verify
            """
        }
        stage('sonarqube') {
            withCredentials([string(credentialsId: 'Sonar_Auth_token', variable: 'SONAR_TOKEN')]) {
                sh """
                    mvn -B -e org.sonarsource.scanner.maven:sonar-maven-plugin:3.6.0.1398:sonar \
                        -Dsonar.sources=$WORKSPACE/src,$WORKSPACE/vars \
                        -Dsonar.tests=$WORKSPACE/test \
                        -Duser.home=$WORKSPACE \
                        -Dsonar.host.url=https://localhost:9000 \
                        -Dsonar.login=$SONAR_TOKEN \
                        -Dsonar.jacoco.reportPath=$WORKSPACE/target/coverage-reports/jacoco.exec
                """
            }
        }
    }
}