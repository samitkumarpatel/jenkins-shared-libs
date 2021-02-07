import net.samittutorial.*

def call() {
    def classInClass = new ClassInClass()
    println(
            classInClass.show(
                    new net.samittutorial.ClassInClass.InnerClass(id: 1, name: "FROM DSL")
            )
    )

}
