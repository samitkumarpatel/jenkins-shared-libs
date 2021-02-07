package net.samittutorial

import spock.lang.Specification

class ClassInClassTest extends Specification{
    def setup(){}
    def "inner class Test"() {
        given:
        def classInClass = new ClassInClass()
        when:
        def p = new ClassInClass.InnerClass(id: 1001, name:"SAMIT")


        then:
        p.id == 1001
        p.name == "SAMIT"
        classInClass.show(p) == "1001 is for : SAMIT"
    }
}
