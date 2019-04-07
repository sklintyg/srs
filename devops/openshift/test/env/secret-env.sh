#!/bin/bash

export CATALINA_OPTS_APPEND="\
-Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
-Djava.library.path=$R_HOME/library/rJava/jri \
-Dfile.encoding=UTF-8"
