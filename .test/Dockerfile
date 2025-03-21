# Use the official WireMock image as the base
FROM wiremock/wiremock:latest

# Expose the default WireMock port
EXPOSE 8080

# Set the entrypoint to run WireMock
ENTRYPOINT ["java", "-cp", "/wiremock/lib/*:/wiremock/extensions/*", "com.github.tomakehurst.wiremock.standalone.WireMockServerRunner"]

# Default command to start WireMock
CMD ["--verbose"]