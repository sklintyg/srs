FROM tomcat:9.0-jre8

RUN apt-get -q update
RUN apt-get -y install git

#Modify owner & permissions
RUN chown root:root $CATALINA_HOME/bin/catalina.sh && chown root:root $CATALINA_HOME/logs
RUN chmod 775 $CATALINA_HOME/bin/catalina.sh && chmod 775 $CATALINA_HOME/logs


#Build
#ARG CACHEBUST=1
RUN cd $CATALINA_HOME/webapps/ && git clone https://github.com/sklintyg/webcert.git && cd webcert && ./gradlew clean build install -PcodeQuality

#wget "https://build-inera.nordicmedtest.se/nexus/service/local/artifact/maven/redirect?r=snapshots&g=se.inera.intyg.webcert&a=webcert&v=LATEST"
#https://build-inera.nordicmedtest.se/nexus/



#Run container


#Hårdkodad version
#RUN cd $CATALINA_HOME/webapps && wget "https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/webcert/webcert/6.4.0.97/webcert-6.4.0.97.jar"


WORKDIR $CATALINA_HOME/bin


#RUN java -jar $CATALINA_HOME/webapps/webcert-6.4.0.97.jar


#Add waitscript for container
#ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.0/wait $CATALINA_HOME/wait
#RUN chmod +x $CATALINA_HOME/wait

RUN cd $CATALINA_HOME/webapps/webcert && ./gradlew appRun
#CMD $CATALINA_HOME/wait && java -Dloader.path=WEB-INF/lib-provided,WEB-INF/lib,WEB-INF/classes -jar $CATALINA_HOME/webapps/web/build/libs/*.war --spring.profiles.active="runtime,it"


EXPOSE 9088