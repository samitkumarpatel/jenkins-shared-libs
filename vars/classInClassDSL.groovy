import net.samittutorial.*
import static net.samittutorial.ClassInClass.*
def call() {
    def classInClass = new ClassInClass()
    println(
            classInClass.show(
                    InnerClass(id: 1, name: "FROM DSL")
            )
    )

}
