package spock.sample.test

class BusinessClass {
    ServiceClass serviceClass

    def businessMethod() {
        return serviceClass.serviceMethod()+" 1"
    }

    def superImportantBusinessMethod(){
        serviceClass.serviceMethodTwo().image("image").inside() {
            "Dummy Value"
        }
    }
}
