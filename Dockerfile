FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# Give Maven wrapper execution permission
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/capstone-0.0.1-SNAPSHOT.jar"]