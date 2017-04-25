#!/bin/bash
MAVEN_OPTS="-Xms256m -Xmx2G -Duser.language=en -Duser.region=UK -server -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000" mvn clean install -Prun