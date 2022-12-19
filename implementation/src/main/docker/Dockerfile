# this Dockerfile creates a container for development purposes of the annotationservice
# it contains the newest annotationService code from the repository as well as anything to build a docker image of the application
# build with: "docker build - < Dockerfile"
FROM jhipster/jhipster
WORKDIR /tib-dev
# get latest code from repo
USER root
RUN mkdir annotationService
RUN git clone https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service.git annotationService
WORKDIR /tib-dev/annotationService
RUN git checkout jhipster-prototype
WORKDIR /tib-dev/annotationService/implementation
RUN rm -R node_modules
RUN npm install



# build application using jib/dockerBuild to create a docker image of the application
#RUN ./mvnw clean package -Pprod verify jib:dockerBuild
RUN ./mvnw package -Pprod verify jib:build -Djib.to.image=registry.gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service:latest -Djib.to.auth.username=<USERNAME> \
 -Djib.to.auth.password=<PASSWORD>