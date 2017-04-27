#!/bin/bash
#MAVEN_OPTS="-Xms256m -Xmx2G -Duser.language=en -Duser.region=UK -server -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000" mvn clean install -Prun

MAVEN_OPTS="-XXaltjvm=dcevm -javaagent:/Users/juliomaqueda/Documents/alfresco/hotswap-agent-1.0.jar -Xms256m -Xmx2G -Duser.language=en -Duser.region=UK -server -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000" mvn install -Prun