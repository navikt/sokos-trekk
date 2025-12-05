FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21-dev@sha256:73bf19b975618df00f4461f7580384abe592df6370050341cc05ab989406cf3e

COPY build/libs/*.jar app.jar

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "app.jar"]