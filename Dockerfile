FROM openjdk:11
ADD ./target/websocket-subscribe-1.0-SNAPSHOT.jar /websoocket-sub-1.0.jar
ENV LANG C.UTF-8
ENV JAVA_OPTS=""
ENV APP_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar /websoocket-sub-1.0.jar  $APP_OPTS" ]
