FROM openjdk:17

COPY target/cloud_storage-0.0.1.jar cloud_storage.jar

ENTRYPOINT ["java", "-jar", "cloud_storage.jar"]