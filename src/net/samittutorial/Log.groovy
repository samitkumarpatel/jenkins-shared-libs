package net.samittutorial

class Log {
    def steps;
    Log(steps) {
        this.steps = steps
    }
    def info(message) {
        steps.echo "INFO : ${message}"
    }
    def debug(message) {
        steps.echo "DEBUG: ${message}"
    }
    static def h(){
        echo "Hello from h static method"
    }
}
