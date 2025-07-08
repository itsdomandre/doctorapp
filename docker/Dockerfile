FROM maven:3.9.7-amazoncorretto-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine
RUN mkdir -p /var/logs
COPY --from=build /build/target/*.jar app.jar
COPY ../../wallet /app/wallet
EXPOSE 8080
ENTRYPOINT ["java","-Xmx256m","-Xms128m","-jar","/app.jar"]
