FROM tomcat:9.0-jdk11-openjdk

LABEL maintainer="SKL SRS" description="SRS Backend"

ENV R_HOME=/usr/lib/R
ENV TZ=Europe/Stockholm

RUN apt-get -q update
RUN apt-get -y install r-base

# Install R packages 'rJava' and 'pch'
# Using version 1.4 of R package 'pch' since version 2.x produces segentation fault on startup.
RUN R CMD javareconf JAVA_HOME=/docker-java-home/jdk
RUN R --quiet -e 'install.packages("rJava", repos="http://cran.us.r-project.org")'
RUN R -e 'packageurl <- "https://cran.r-project.org/src/contrib/Archive/pch/pch_1.4.tar.gz"; \
          install.packages(packageurl, repos=NULL, type="source")'

#Modify owner & permissions
RUN chown root:root $CATALINA_HOME/bin/catalina.sh && chown root:root $CATALINA_HOME/logs
RUN chmod 775 $CATALINA_HOME/bin/catalina.sh && chmod 775 $CATALINA_HOME/logs

#Build srs backend
COPY . $CATALINA_HOME/webapps/
RUN cd $CATALINA_HOME/webapps/ && ./gradlew clean build -x test

#Add waitscript for container
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.0/wait $CATALINA_HOME/wait
RUN chmod +x $CATALINA_HOME/wait

#Run container
WORKDIR $CATALINA_HOME/bin
CMD $CATALINA_HOME/wait && java \
  -Djava.library.path="/usr/local/lib/R/site-library/rJava/jri" \
  -Dloader.path=WEB-INF/lib-provided,WEB-INF/lib,WEB-INF/classes\
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005\
  -jar $CATALINA_HOME/webapps/web/build/libs/*.war \
  --spring.profiles.active="runtime,it,docker"

EXPOSE 8080
EXPOSE 5005
