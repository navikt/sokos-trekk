FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21-dev@sha256:841ce180b7956a9693b5f8b578ea21d6b5bb710e4aa15d4d1c06839cffa7761c

COPY build/libs/*.jar app.jar

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "app.jar"]