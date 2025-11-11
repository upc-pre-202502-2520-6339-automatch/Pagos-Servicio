# Etapa 1: Compilar el proyecto con Maven
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar archivos necesarios para Maven
COPY pom.xml .
COPY src ./src

# Empaquetar sin ejecutar tests
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar la aplicación con JDK 17
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copiar el JAR generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Copiar explícitamente el archivo de configuración actualizado
COPY src/main/resources/application.properties /app/application.properties

# Exponer el puerto
EXPOSE 8080

# Forzar perfil docker para asegurarte de que se use el archivo correcto
ENV SPRING_CONFIG_LOCATION=/app/application.properties
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]