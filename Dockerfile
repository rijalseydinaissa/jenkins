# Étape de build : Compile avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests  # Skip tests car déjà faits dans Jenkins

# Étape runtime : Image légère avec JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/jenkins-demo.jar app.jar

# Expose le port (Render utilise $PORT dynamiquement)
EXPOSE 8080

# Démarrage : Utilise $PORT de Render et options JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT ${JAVA_OPTS:-'-Xmx1024m -Xms512m'} -jar app.jar"]