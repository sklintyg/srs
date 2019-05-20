FROM tomcat:9.0-jre8

LABEL maintainer="SKL SRS" \
      description="SRS Backend"


# Install JDK & R
RUN apt-get -q update
RUN apt-get -y install openjdk-8-jdk
RUN apt-get -y install r-base

#Env variables
ENV R_HOME=/usr/lib/R
#RUN echo $R_HOME
ENV TZ=Europe/Stockholm

#Install rJava
RUN R CMD javareconf JAVA_HOME=/docker-java-home/jre
RUN R --quiet -e 'install.packages("pch", repos="http://cran.us.r-project.org")'
RUN R --quiet -e 'install.packages("rJava",repos="http://cran.us.r-project.org")'


#Modify owner & permissions
RUN chown root:root $CATALINA_HOME/bin/catalina.sh && chown root:root $CATALINA_HOME/logs
RUN chmod 775 $CATALINA_HOME/bin/catalina.sh && chmod 775 $CATALINA_HOME/logs

#Build srs backend
#ARG CACHEBUST=1
COPY ./ $CATALINA_HOME/webapps/
RUN cd $CATALINA_HOME/webapps/ && ./gradlew clean build

#Run container
WORKDIR $CATALINA_HOME/bin

#Add waitscript for container
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.0/wait $CATALINA_HOME/wait
RUN chmod +x $CATALINA_HOME/wait

CMD $CATALINA_HOME/wait && java -Djava.library.path="/usr/local/lib/R/site-library/rJava/jri" -Dloader.path=WEB-INF/lib-provided,WEB-INF/lib,WEB-INF/classes -jar $CATALINA_HOME/webapps/web/build/libs/*.war --spring.profiles.active="runtime,it,bootstrap,scheduledUpdate"


EXPOSE 8080