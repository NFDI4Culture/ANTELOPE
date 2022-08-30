#!/bin/bash

#this script start the build process of jhipster via maven (./mvnw) and start the backend application. 
# the spring boot parameters starting spring boot in debug mode, listening on port 5005 for debugger attachers
# e.g. you can use the following debug config in vscode to attach a debugger and debug the backend services from your ide:

#{
#            "type": "java",
#            "name": "annotationService backend",
#            "request": "attach",
#            "hostName": "localhost",
#            "port": "5005"
#        }

./mvnw -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
