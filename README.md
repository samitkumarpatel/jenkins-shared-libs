## Jenkins Shared Library
 
- This repo contains the shared libraries for running a Jenkins pipeline.
- The libraries are written in Groovy and should follow the standard practices of the language. For further details on the syntax and standards, use the following http://groovy-lang.org/syntax.html.
- The junit are written in apache Spock and should be follow the standard practice of the language. For more details http://spockframework.org/spock/docs/1.3/all_in_one.html
- Libraries have been generated to follow the naming standard of tech stack and build step as much as possible

# IDE Setup To Work With Jenkinsfile

### Intellij
[for more information](http://vgaidarji.me/blog/2018/07/30/working-with-jenkinsfile-in-intellij-idea/)

* Create a file called Jenkinsfile in the root folder of project
* Open that project in Intellij IDE
* Associate Jenkinsfile in Intellij as groovy
* Tips to associate Jenkinsfile in Intellij
* `Intellij IDEA` - `Preferences` - `Editor` - `File Types` - select groovy from `Recognized File Type` -  select `+` from `Registered Patterns` and add Jenkifile init - `save`
* Configure Groovy SDK to enable autocompletion in Jenkinsfile
* Use `pipeline.gdsl` to have Pipeline syntax autocompletion in Jenkinsfile
* process to get `pipeline.gdsl` follow below steps
* install Jenkins and install pipeline plugins
* create a pipeline job - save that job
* access the job url (http://localhost:8080/job/pipeline-job-you-just-created/pipeline-syntax/)
* click on ` IntelliJ IDEA GDSL` - copy that content
* place pipeline.gdsl somewhere in src folder in your project so that it’s recognized properly
* add pipeline.gdsl to .gitignore to reduce noise in the repo

If the autocompletion does not work, follow steps
* Creating a folder /src/main/groovy, putting the file in there and marking it as a sources root (right click on the folder -> Mark directory as -> Sources Root) did the trick.
* File > New > Project from Existing Sources…), a message popped up: DSL descriptor file has been change and isn’t currently executed.
