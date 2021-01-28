package net.samittutorial
class Math implements Serializable {
    def pipeline 
    Math(def pipeline) {
        this.pipeline = pipeline
    }
    Math() {

    }
    def writeAndDisplayContent(int x, int y) {
        sh """
            echo ${x+y} > ${pipeline.env.WORKSPACE}/result.txt
            cat ${pipeline.env.WORKSPACE}/result.txt
        """
    }

    static def substract(int x, int y) {
        return x - y
    }

    static def add(int x, int y) {
        return x + y
    }

    static def info() {
        return "Hello from Math class"
    }
}