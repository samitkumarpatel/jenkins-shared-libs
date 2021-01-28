package net.samittutorial
class Math implements Serializable {

    static def writeAndDisplayContent(int x, int y) {
        sh """
            echo ${x + y} > ${env.WORKSPACE}/result.txt
            cat ${env.WORKSPACE}/result.txt
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