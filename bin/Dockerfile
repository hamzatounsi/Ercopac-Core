FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

EXPOSE 10000

CMD sh -c 'java -Dserver.port=${PORT:-10000} -jar target/ercopac-tracker-0.0.1-SNAPSHOT.jar'