# Antelope Development

> ↳ [Project README](../README.md)

Antelope is powered by [JHipster](https://www.jhipster.tech/documentation-archive/v7.9.3) and [Angular](https://angular.io/docs).

## Prerequisites

- [Node.js](https://nodejs.org/en) v17+ (ships with *NPM* by default)
- [Node Package Manager (NPM)](https://www.npmjs.com/) v9+
- [Java Runtime Environment (JRE)](https://www.java.com/en/download/manual.jsp) v16+
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) v22+

## Installation

1. Check out the project from [GitLab](https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service).

2. Install dependencies

``` console
npm install
```

3. install iart:
- checkout iart project (branch: ray_serve) from [Gitlab](https://github.com/TIBHannover/iart/tree/ray_serve)
- follow the iare readme for setup (run install.sh)
- set environment var to iart home dir (needed to build antelope via docker-compose): 
$> IART_HOME=/home/kolja/git/iart2
$> export IART_HOME

## Project Structure

The root directory contains organizational project related files. Application related files reside below the `/implementation` directory. The source directory `./src` structure follows the common *Java* project file structure. Moreover, the *Angular* web application exists at `./src/main/webapp`.  
  
*JHipster* generates and reads configuration files for tools like *git*, *prettier*, *eslint*, *husky*, and others (you can find references in the web):

- **./.yo-rc.json** - *Yeoman* configuration file storing *JHipster* configuration with the `generator-jhipster` key. You may find `generator-jhipster-*` for specific blueprints configuration.
- **./.yo-resolve** (optional) - *Yeoman* conflict resolver allowing to use a specific action when conflicts are found skipping prompts for files that matches a pattern. Each line should match `[pattern] [action]` with pattern been a [Minimatch](https://github.com/isaacs/minimatch#minimatch) pattern and action been one of skip (default if ommited) or force.
- **./.jhipster/*.json** - *JHipster* entity configuration files
- **./npmw** - Wrapper for using locally installed *NPM*. *JHipster* installs *Node.js* and *NPM* locally using the build tool by default. This wrapper makes sure *NPM* is installed locally and uses it avoiding possible issues due to different development versions.
- **./src/main/docker** - Docker configurations for the application and services that the application depends on.

## Development

1. Generate one branch per issue:

``` console
git checkout main
git checkout -b issue<ISSUE_NR>_<BRANCHNAME>
```

2. Implement issue objective.

3. `optional` Write tests.

4. Debug application:

``` console
./mvnw
```
``` console
npm start
```

> The build process is managed via *NPM* abstracting [Angular CLI](https://angular.io/cli) with [Webpack](https://webpack.js.org/). Run the following commands in two separate console windows in order to obtain a blissful development experience.

5. Check build under productive circumstances, including to linters, tests and audits:

``` console
./mvnw package -Pprod verify jib:build 
```

**IF** the previous step terminated with failure, repeat from implementation (2.).

6. Commit changes with a proper message.

7. Merge issue branch to test branch and check functionality again (4., 5.):

``` console
git checkout test
git fetch
git pull
git merge issue<ISSUE_NR>_<BRANCHNAME>
```

> The `npm run` command helps with listing all available scripts provided for the project.

### Client Tests

Unit testing is powered by [Jest](https://jestjs.io/). Test files are located in `src/test/javascript/`. The test suite can be run through the following *NPM* script:

``` console
npm test
```

### JHipster Control Center

The *JHipster Control Center* helps with managing the application. It can be started for a local instance as follows:

``` console
docker-compose -f src/main/docker/jhipster-control-center.yml up
```

The *Control Center* will be available on http://localhost:7419`.

### API-first with OpenAPI

The project is configured for an [OpenAPI-Generator](https://github.com/OpenAPITools/openapi-generator). It can help with generating API code based on the definitions located in `src/main/resources/swagger/api.yml`. To use it, run:

``` console
./mvnw generate-sources
```

> To edit the definitions file, the *Swagger-Editor* can help. It can be started for a local instance using *Docker*: `docker-compose -f src/main/docker/swagger-editor.yml up -d`. The editor will be available at http://localhost:7742.

## Deployment

The deployment requires the following steps, spreading across **TEST** and **PROD** servers.

### TEST Server

1. Push changes on test branch to the remote, *GitLab* will rebuild the docker container automatically:

``` console
git push
```

2. Check for build progress on https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/-/jobs. If the job creation fails, you may build and publish locally:

``` console
./mvnw package -Pprod verify jib:build -Djib.to.image=registry.gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/test:latest -Djib.to.auth.username=<USERNAME> -Djib.to.auth.password=<PASSWORD>
```

3. Check for rebuilt container on https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/container_registry/4057458.


4. Start deploy job `deploy TEST-annotationservice` in the deploy project on https://git.tib.eu/nfdi4culture/annotation_service-deploy/-/pipelines/49416.


5. Check proper functionality on http://nfdi4cultureann11.test.service.tib.eu:8080/ (note that the staging application is only accessible from a *TIB* internal network).

### PROD Server

1.  Merge test branch to main branch and check:

``` console
git checkout main
git fetch
git pull
git merge test
```

2. Check for build progress on https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/-/jobs.

3. Check for rebuilt container on https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/container_registry/3734515.

4. Start deploy job `deploy PROD-annotationservice` in deploy project on https://git.tib.eu/nfdi4culture/annotation_service-deploy/-/pipelines/49416.

5. Check proper functionality on https://service.tib.eu/annotation/.

6. Delete isse branch.

### Docker

To build a *Docker* container image of the application, run one of the following commands (depending on the target environment):

``` console
docker build - < src/main/docker/Dockerfile_PROD
```

``` console
docker build - < src/main/docker/Dockerfile_TEST
```

> The *Dockerfile* is capable to push the image into the annotation service container registry. See *Dockerfile* comments for further instructions.

To start the application via docker, run:

``` console
docker-compose -f src/main/docker/app.yml up
```

### Packaging

#### As `.jar`

To build a `.jar` file and optimize the annotation service client application resources for production, run:

``` console
./mvnw -Pprod clean verify
```

To ensure everything worked, subsequently type:

``` console
java -jar target/*.jar
```

Check on http://localhost:8080 for the results.

#### As `.war`

To package your application as a `.war` in order to deploy it to an application server, run:

``` console
./mvnw -Pprod,war clean verify
```

### Testing

To launch application tests, run:

``` console
./mvnw verify
```

### Code quality

[Sonar](https://www.sonarsource.com/) provides code quality analysis capabilites. A local *Sonar* server can be run as stated below:

``` console
docker-compose -f src/main/docker/sonar.yml up -d
```

The analysis interface will be available on http://localhost:9001.

> Authentication is turned off in `src/main/docker/sonar.yml` to ensure an out-of-the-box experience while trying out *SonarQube*, for real use cases turn it back on.

A *Sonar* analysis can be run using [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner), or the related *Maven* plugin. Given this as a prerequisite, run:

``` console
./mvnw -Pprod clean verify sonar:sonar
```

If the *Sonar* phase needs to be re-run, please be sure to specify at least the `initialize` phase. Since *Sonar* properties are loaded from the `sonar-project.properties` file, this is required.

``` console
./mvnw initialize sonar:sonar
```

## Integration with Third-party Applications

A major contribution of Antelope is supposed to be the integration from third-party applications. That being said, an integration into other web based applications can happen in two ways.

### Via `iframe`

The [HTML Example](./src/main/examples/html/index.html) gives a complete example on how to integrate Antelope via an `iframe` element. The example reads a textfield and sends it to the annotation service for entity mapping. Then, the annotation service will display the results in a select component within the `iframe`. When the user selects a result within the iframe, a message is send to the parent application. The message contains the selected entity data and is processed within the function `onMessage()`.

> See the related schema graph at `src/main/examples/webpage/integrationConcept.svg`.

### Via `RESTful API`

More specific integration concepts with the annotation service can be achieved thorugh the provided *REST* compliant API. The API documentation is available on `https://service.tib.eu/annotation/v3/api-docs/openapi`.

---

<sub>&copy; TIB Hannover</sub>