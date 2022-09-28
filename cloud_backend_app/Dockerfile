FROM adoptopenjdk:11-jdk-hotspot
EXPOSE 8070
ADD target/cloud_backend_app-0.0.1-SNAPSHOT.jar CloudBackendApp.jar
ENTRYPOINT ["java", "-jar", "CloudBackendApp.jar"]