# syntax=docker/dockerfile:1

# =====================================================================
# Etapa 1: compilacion con Maven + JDK 17
# =====================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Cache de dependencias: solo se invalida si cambia el pom.xml
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Codigo fuente y empaquetado
COPY src ./src
RUN mvn -B clean package -DskipTests

# =====================================================================
# Etapa 2: runtime con JRE 17 Alpine (imagen final liviana)
# =====================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="prestamoservice" \
      org.opencontainers.image.description="PrestaControl API REST - Spring Boot 3 + JWT + PostgreSQL" \
      org.opencontainers.image.vendor="com.fernandez_terrones" \
      org.opencontainers.image.version="1.0.0"

WORKDIR /app

# Usuario sin privilegios
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar /app/app.jar
RUN chown -R spring:spring /app

USER spring:spring

# Render inyecta PORT; en local se usa 8080 por defecto
EXPOSE 8080

# -Dserver.port=${PORT:-8080} garantiza el bind al puerto que asigna Render
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
