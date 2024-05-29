# Antelope Development

Antelope is powered by a [JHipster](https://www.jhipster.tech/documentation-archive/v7.9.3)-scaffolded Java backend, as well as a custom [Vue](https://vuejs.org/) frontend.

## Prerequisites

- [Node.js](https://nodejs.org/en) v17+ (ships with NPM by default)
- [Node Package Manager (NPM)](https://www.npmjs.com/) v9+
- [Java Runtime Environment (JRE)](https://www.java.com/en/download/manual.jsp) v16+
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/) v22+

## Installation

1. Check out the project from [GitLab](https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service).

2. Install dependencies

``` console
npm install -ws
```

3. Install iart:

- Checkout iart project (branch: `ray_serve`) from [GitLab](https://github.com/TIBHannover/iart/tree/ray_serve)
- Follow the iart README for setup (run install.sh)
- Set environment variable to iart home dir (needed to build antelope via docker-compose): `export IART_HOME=/home/<user>/git/iart2`

## Project Structure

The application consists of a separate backend and frontend application. The backend application is a Java Spring application that manages its dependencies with Maven and JHipster. The frontend application is communicates with the backend via a HTTP API.

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
npm run start:dev
```

> Alternatively the **backend** or **frontend**  can optionally be started in isolation appending `-w <backend|frontend>`.

> 🛠️ The static build version of the current development state is served from http://localhost:8080. To debug the frontend application with hot module replacement open http://localhost:9000 instead.

5. Check build under productive circumstances, including to linters, tests and audits:

``` console
npm run check:prod -w backend 
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

### Frontend Tests

Unit testing is powered by [Jest](https://jestjs.io/). Test files are located in `src/test/javascript/`. The test suite can be run through the following *NPM* script:

``` console
npm run test -w frontend
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
npm run openapi -w backend
```

> To edit the definitions file, the *Swagger-Editor* can help. It can be started for a local instance using *Docker*: `docker-compose -f src/main/docker/swagger-editor.yml up -d`. The editor will be available at http://localhost:7742.

## Deployment

The deployment requires the following steps, spreading across **TEST** and **PROD** servers.

### Update Vecner Container

If the Vecner implementation has changed, the project must be rebuild and deployed to the GitLab container registry.

``` cli
docker build -t registry.gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/vecner:latest <VECNER_PATH>
docker push registry.gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/vecner:latest
```

> See https://docs.gitlab.com/ee/user/packages/container_registry/build_and_push_images.html for more details.

### TEST Server

1. Push changes on test branch to the remote, *GitLab* will rebuild the docker container automatically:

``` console
git push
```

2. Check for build progress on https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/-/jobs. If the job creation fails, you may build and publish locally:

``` console
cd ./backend/ && ./mvnw package -Pprod verify jib:build -Djib.to.image=registry.gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/test:latest -Djib.to.auth.username=<USERNAME> -Djib.to.auth.password=<PASSWORD>
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
git push
```

2. Check for build progress on https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/-/jobs.

3. Check for rebuilt container on https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/container_registry/3734515.

4. Start deploy job `deploy PROD-annotationservice` in deploy project on https://git.tib.eu/nfdi4culture/annotation_service-deploy/-/pipelines/49416.

5. Check proper functionality on https://service.tib.eu/annotation/.

6. Delete isse branch.

### server maintenance

on the server, antelope is stared via a systemd service. the service file is located at /etc/systemd/system/annotation.service
The service can be controled via the following commands:
sudo systemctl status annotation.service
sudo systemctl start annotation.service
sudo systemctl restart annotation.service
sudo systemctl stop annotation.service

the log file can be found at 
/var/log/antelope-err.log
/var/log/antelope.log

the log file is written continuosly, to check last results  (here last 500 lines) use:

sudo tail /var/log/antelope.log -n 500

the docker images need space on disk. if an "out of disk" space occurs, you may free disk space by removing unused (old) docker images and volumes:

docker system prune
docker volume rm <VOLUME NAME>

### Docker

To build a *Docker* container image of the application, run one of the following commands (depending on the target environment):

``` console
docker build - < ./backend/src/main/docker/Dockerfile_PROD
```

``` console
docker build - < ./backend/src/main/docker/Dockerfile_TEST
```

> The *Dockerfile* is capable to push the image into the annotation service container registry. See *Dockerfile* comments for further instructions.

To start the application via docker, run:

``` console
docker-compose -f ./backend/src/main/docker/app.yml up
```

### Packaging

#### As `.jar`

To build a `.jar` file and optimize the annotation service client application resources for production, run:

``` console
npm run build:jar -w backend
```

To ensure everything worked, subsequently type:

``` console
java -jar ./target/*.jar
```

Check on http://localhost:8080 for the results.

#### As `.war`

To package your application as a `.war` in order to deploy it to an application server, run:

``` console
cd ./backend/ && ./mvnw -Pprod,war clean verify
```

### Backend Tests

To launch backend tests, simply run:

``` console
npm run test -w backend
```

### Code quality

[Sonar](https://www.sonarsource.com/) provides code quality analysis capabilites. A local *Sonar* server can be run as stated below:

``` console
docker-compose -f ./backend/src/main/docker/sonar.yml up -d
```

The analysis interface will be available on http://localhost:9001.

> Authentication is turned off in `src/main/docker/sonar.yml` to ensure an out-of-the-box experience while trying out *SonarQube*, for real use cases turn it back on.

A *Sonar* analysis can be run using [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner), or the related *Maven* plugin. Given this as a prerequisite, run:

``` console
cd ./backend/ && ./mvnw -Pprod clean verify sonar:sonar
```

If the *Sonar* phase needs to be re-run, please be sure to specify at least the `initialize` phase. Since *Sonar* properties are loaded from the `sonar-project.properties` file, this is required.

``` console
cd ./backend/ && ./mvnw initialize sonar:sonar
```

## Integration with Third-party Applications

A major contribution of Antelope is supposed to be the integration from third-party applications. That being said, an integration into other web based applications can happen in two ways.

### Via `iframe`

The [HTML Example](../frontend/examples/iframe/index.html) gives a complete example on how to integrate Antelope via an `iframe` element. The example reads a textfield and sends it to the annotation service for entity mapping. Then, the annotation service will display the results in a select component within the `iframe`. When the user selects a result within the iframe, a message is send to the parent application. The message contains the selected entity data and is processed within the function `onMessage()`.

> See the related schema graph at `src/main/examples/webpage/integrationConcept.svg`.

### Via `HTTP API`

More specific integration concepts with the annotation service can be achieved through the provided HTTP API. The API documentation is available on `https://service.tib.eu/annotation/api.htm`.

---

<sub>&copy; TIB Hannover</sub>