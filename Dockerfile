FROM ubuntu:jammy
RUN apt-get update && apt-get install -y openjdk-21-jre-headless ca-certificates && rm -rf /var/lib/apt/lists/*
COPY server/target/diet-butler-server-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dcom.sun.net.ssl.checkRevocation=false", "-jar", "/app.jar"]