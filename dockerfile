FROM openjdk:8
ADD target/w3s-1.0.0.jar /app.jar
ADD so.zip /so.zip
ENV LANG C.UTF-8
ENV JAVA_OPTS=""
ENV APP_OPTS=""
EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar /app.jar  $APP_OPTS" ]
