FROM java:alpine
VOLUME /tmp
ADD build/libs/${projectName}.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","-Xmx200m”,”/app.jar"]
HEALTHCHECK --interval=10s --timeout=3s CMD curl -f http://localhost:8080/health || exit 1
EXPOSE 8080