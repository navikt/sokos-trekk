FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:84cdaea917aeb5ea9e66a880e33ce209aed951df8074b9ba07b5d06fdfc3d9d2

COPY build/libs/*.jar app.jar

ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "app.jar"]