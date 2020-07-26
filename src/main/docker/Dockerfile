FROM 1science/java
MAINTAINER sblackmon@apache.org
LABEL Description="probot"
WORKDIR /
ADD maven/jetty.xml /jetty.xml
ADD maven/logback.xml /logback.xml
ADD maven/microservice.cfg /microservice.cfg
ADD maven/probot-jar-with-dependencies.jar /probot-jar-with-dependencies.jar
ADD start.sh /start.sh
RUN chmod +x /start.sh
EXPOSE 10000
ENTRYPOINT ["/start.sh"]
CMD [ \
    "java", \
    "-Dconfig.url=file:///application.conf", \
    "-Dlogback.configurationFile=file:///logback.xml", \
    "-cp", \
    "/probot-jar-with-dependencies.jar", \
    "probot.ProbotMicroservice", \
    "microservice.cfg" \
    ]
