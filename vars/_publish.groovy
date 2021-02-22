def call(body={}) {
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    pipeline {
        agent any;
        stages {
            stage('build') {
                steps {
                    echo "BUILD"
                }
            }
            stage('unitest') {
                when {
                    anyOf {
                        equals expected: true, actual: pipelineParams.isEmpty();
                        equals expected: false, actual: pipelineParams.skipUnitest
                    }  
                }
                steps {
                    echo "UNITEST"
                }
            }
        }
    }
}