## Java EE Jenkins Shared Library
 
- This repo contains the shared libraries for running a Jenkins pipeline.
- The libraries are written in Groovy and should follow the standard practices of the language. For further details on the syntax and standards, use the following http://groovy-lang.org/syntax.html.
- The junit are written in apache Spock and should be follow the standard practice of the language. For more details http://spockframework.org/spock/docs/1.3/all_in_one.html
- Libraries have been generated to follow the naming standard of tech stack and build step as much as possible

####about the folder structure

```
.
├── resources
├── src
│   └── net
│       └── apmoller
│           └── cdpipline
│               └── javaeelibs
├── test
│   ├── net
│   │   └── apmoller
│   │       └── cdpipeline
│   │           └── javaeelibs
│   └── spock
│       └── sample
│           └── test
└── vars
```


`resource` : Folder should contain any static commonly used file.

`src` : Groovy class , which can contain pipeline code.

`test` : Junit class for unitest.

`vars` : will contain pipeline script which will be available in the Jenkins global scope.


##About Groovy Class on src/

| Class   |      Purpose      |
|-------------------|:-------------:|
| Git.groovy        |To deal with any git command related activity like git checkout, tag, commit files and etc |
| Maven.groovy      |To deal with all the maven activity like build, versioning. You can specify build execution environment like Docker, Jenkins or Other  |
| SonarQube.groovy  |To deal with Sonar scan, quality gate check and etc |
| Utility.groovy    |This class is a wrapper for jenkins provided pipeline and all other common reusable functionality |
 
## How to use

- To Build and run the test

```
mvn clean install
```

- To run the test

```
mvn test
```