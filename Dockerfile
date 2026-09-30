# --- build stage: compile and package the jar ---
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
# download dependencies in a separate layer, so code changes do not re-download them
RUN ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests

# --- runtime stage: only the JRE and the jar ---
FROM eclipse-temurin:24-jre
WORKDIR /app
RUN useradd --system --no-create-home app
USER app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
