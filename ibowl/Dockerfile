# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN ./mvnw clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:17-jre as runtime


RUN useradd -r -d /app -s /sbin/nologin appuser

WORKDIR /app

# Copie uniquement le jar final (nom dynamique)
COPY --from=build /build/target/*.jar app.jar
# Permissions minimales
RUN chown appuser:appuser app.jar && chmod 500 app.jar
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
# Healthcheck sur /actuator/health
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
  CMD wget --spider -q http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"] 