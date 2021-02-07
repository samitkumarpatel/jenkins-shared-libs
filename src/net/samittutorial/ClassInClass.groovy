package net.samittutorial

class ClassInClass {
    def show(InnerClass innerClass) {
        return innerClass.id+" is for : "+innerClass.name
    }

    static class InnerClass {
        int id
        String name
    }
}