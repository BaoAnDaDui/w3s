FROM openjdk:11
ADD ./target/websocket-subscribe-1.0.jar /websocket-subscribe-1.0.ja
ENV LANG C.UTF-8
ENV JAVA_OPTS=""
ENV APP_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar /websocket-subscribe-1.0.ja  $APP_OPTS" ]
