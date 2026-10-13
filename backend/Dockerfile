FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S geoteacher && adduser -S geoteacher -G geoteacher
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER geoteacher
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
