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
                    equals expected: false, actual: pipelineParams.skipUnitest 
                }
                steps {
                    echo "UNITEST"
                }
            }
        }
    }
}