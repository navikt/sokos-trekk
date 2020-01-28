FROM navikt/java:8

ARG SPRING_PROFILES_ACTIVE
RUN echo ${SPRING_PROFILES_ACTIVE}


COPY trekk-app/target/trekk-*.jar app.jar
COPY init-scripts /init-scripts
ENV JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=default,${SPRING_PROFILES_ACTIVE}"
