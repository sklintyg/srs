FROM tomcat:9.0

LABEL maintainer="SKL SRS" \
      description="SRS Backend"

#ENV CATALINA_HOME /opt/tomcat
COPY ./web/build/libs/*.war /tmp/webapps/app.war

#ADD tomcat/bin $CATALINA_HOME/bin/

RUN chmod 775 $CATALINA_HOME/bin/catalina.sh
RUN chmod 775 $CATALINA_HOME/logs
RUN chown root:root $CATALINA_HOME/bin/catalina.sh
RUN chown root:root $CATALINA_HOME/logs

RUN cp -r /tmp/webapps/* $CATALINA_HOME/webapps/

WORKDIR $CATALINA_HOME/bin

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.0/wait $CATALINA_HOME/wait
RUN chmod +x $CATALINA_HOME/wait

ENV JAVA_OPTS ""

CMD $CATALINA_HOME/wait && $CATALINA_HOME/bin/catalina.sh run $JAVA_OPTS && touch $CATALINA_HOME/logs/myapp.log && tail -f $CATALINA_HOME/logs/myapp.log

EXPOSE 8097