FROM navikt/java:8

WORKDIR /app

COPY trekk-app/target/trekk-*.jar app/app.jar