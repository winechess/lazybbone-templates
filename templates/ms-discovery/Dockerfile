FROM java:alpine
VOLUME /tmp
ADD build/libs/${projectName}.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE ${serverPort}