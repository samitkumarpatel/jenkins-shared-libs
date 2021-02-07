import net.samittutorial.ClassInClass

def call() {
    def classInClass = new ClassInClass()
    print classInClass.show(
                    new ClassInClass().InnerClass(id: 1, name: "FROM DSL")

    )
}