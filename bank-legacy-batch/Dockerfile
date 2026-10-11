FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
# Copiamos el ejecutable compilado
COPY target/*.jar app.jar
# Copiamos la carpeta de recursos físicos para el FileSystemResource
COPY src/main/resources/data/ /src/main/resources/data/
ENTRYPOINT ["java","-jar","/app.jar"]