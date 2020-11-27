FROM navikt/java:8

COPY trekk-app/target/trekk-*.jar app.jar
COPY init-scripts /init-scripts
