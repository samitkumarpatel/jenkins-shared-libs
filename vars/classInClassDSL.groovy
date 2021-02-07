import net.samittutorial.*
import static net.samittutorial.ClassInClass.InnerClass
def call() {
    def classInClass = new ClassInClass()
    println(
            classInClass.show(
                    new ClassInClass.InnerClass(id: 1, name: "FROM DSL")
            )
    )

}
