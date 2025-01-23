# loan-syndication-marketplace-backend

## Run Loan Marketplace backend repo in VS Code:

Before run the project you need to have installed:

  * [Gradle](https://gradle.org/install/) for build the project  
  * [JDK and JRE](https://code.visualstudio.com/docs/languages/java) version 17 or higher
  * [Java and Spring extensions](https://code.visualstudio.com/docs/java/extensions) for VS Code

## We have 2 ways to run the project locally

  1. Use the previously installed Spring extension for VS Code
  2. Using gradle:
      1. In a command console (CMD, VS Code terminal, ...) located in the project directory run the next commmand:  `gradle tasks`
      2. Run: `gradle build` or `./gradlew build`
      3. Finally, you need to run the jar file in /build/libs/*.jar

## Hot Swapping

  1. Using VS Code:
     1. No special configuration is needed, just run the project with the Spring extension
  2. Using IntelliJ:
     1. Enable the option `Build project automatically` in `Settings > Build, Execution, Deployment > Compiler`
     2. Allow auto-make to start in `Settings > Advanced Settings > Registry`

## Run the project in Docker:

  1. Install [Docker](https://docs.docker.com/desktop/?_gl=1*wujekh*_ga*ODE2OTc5NDcxLjE2OTE2ODI4MTI.*_ga_XJWPQMJYHQ*MTY5MjIxNjc1Ny41LjEuMTY5MjIxODQ3Ni4yMC4wLjA.) in your machine
  2. In a command console (CMD, Vs Code Terminal, ...) located in the project directory run the next command in order to build the image: `docker build --tag 'tag' . `
  3. Finally, run the image built: `docker run -d -p port:10800 'tag'`

## REST API

### Swagger

A Swagger API documentation and client exists for the REST API.  It's available in every environment except for production using the path `/swagger-ui/index.html` eg for `qa`:

https://qa.laminafs.com/swagger-ui/index.html

