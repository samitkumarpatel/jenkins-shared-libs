package net.samittutorial
class Math implements Serializable {
    def pipeline
    
    Math(def pipeline){
        this.pipeline = pipeline
    }

    Math() {

    }

    def addAndWriteTheResultToAFile(int x, int y) {
        pipeline.sh """
            echo ${x + y} > ${pipeline.env.WORKSPACE}/result.txt
            cat ${pipeline.env.WORKSPACE}/result.txt
        """
    }

    def substract(int x, int y) {
        return x - y
    }

    def add(int x, int y) {
        return x + y
    }

    static def info() {
        echo "Hello from Math class"
    }
}