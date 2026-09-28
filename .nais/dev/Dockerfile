FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21-dev@sha256:8d94b4a4d95dc9f1bdc9b0d10942c9247725017df9f79ccbdb1b0012dc36fb47

COPY build/libs/*.jar app.jar

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "app.jar"]