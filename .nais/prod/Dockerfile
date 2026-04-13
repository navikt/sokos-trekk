FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:d6b729bc7e9ec9198b1af32c8a10a69a9c6b2ec48b84c4f7f1b36f45334ac137

COPY build/libs/*.jar app.jar

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "app.jar"]