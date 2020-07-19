package spock.sample.test

import spock.lang.Specification

class SampleSpockTest extends Specification {

    def "Spock test 01"(){
        given:
        def x
        when:
        x = 1+1
        then:
        assert x == 2
    }

    def "Spock test 02"(){
        when:
        def x = Math.max(1, 2)

        then:
        x == 2
    }

    def "Spock test 03 - expect block"(){
        expect:
        Math.max(1, 2) == 2
    }

    def "Spock test 04 - Exception assertion"() {
        given:
        def map = new HashMap()

        when:
        map.put(null, "elem")

        then:
        notThrown(NullPointerException)
    }

    def "Spock test 05 mocking01"() {
        given:
        def serviceClass = Stub(ServiceClass.class)
        def businessClass = new BusinessClass(serviceClass: serviceClass)

        and:
        serviceClass.serviceMethod() >> "HELLO"

        when:
        def r = businessClass.businessMethod()

        then:
        assert r == "HELLO 1"
    }

    def "Spock test 06 - mocking02 "(){
        given:
        ServiceClass serviceClass = Stub({
            serviceMethodTwo() >> [image: { String _args -> [inside: { "SOMETHING" }]}]
        })
        def businessClass = new BusinessClass(serviceClass: serviceClass)

        when:
        def returnVal = businessClass.superImportantBusinessMethod()

        then:
        returnVal == "SOMETHING"
    }
}