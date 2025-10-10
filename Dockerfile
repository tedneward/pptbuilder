# Start from latest Ubuntu
FROM ubuntu:latest

# Small runtime image: install OpenJDK 17 only
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jdk ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy a pre-built fat JAR into the image. The build should produce the jar at build/libs/<name>.jar
# When building the image, ensure the JAR is present in the build context, for example:
#   cp build/libs/<artifact>.jar ./pptbuilder.jar
#   docker build -t pptbuilder:local .
COPY ./build/libs/pptbuilder.jar /app/pptbuilder.jar

# Expose a mount point for presentations
VOLUME ["/presentations"]

WORKDIR /presentations

# Run the provided jar with any CLI args passed to the container
ENTRYPOINT ["sh", "-c", "java -jar /app/pptbuilder.jar \"$@\"", "--"]

# Example usage (host PowerShell):
# 1) Build the fat jar locally:
#    ./gradlew.bat shadowJar
# 2) Build the image:
#    docker build -t pptbuilder:local .
# 3) Run with your slides directory mounted:
#    docker run --rm -it -v /mnt/c/Users/Ted/Projects/Presentations:/presentations pptbuilder:local Keynotes/EmbracingHistory.xmlmd -f slidy --verbosity info
#    ... or ...
#    docker run --rm -it -v C:\Users\Ted\Projects\Presentations:/presentations pptbuilder:local Keynotes/EmbracingHistory.xmlmd -f pptx --verbosity info
