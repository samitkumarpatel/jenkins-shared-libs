def call(body={}) {
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    pipeline {
        stages {
            stage('build') {
                steps {
                    echo "BUILD"
                }
            }
            stage('unitest') {
                when { expression { return pipelineParams.unitest } }
                steps {
                    echo "UNITEST"
                }
            }
        }
    }
}