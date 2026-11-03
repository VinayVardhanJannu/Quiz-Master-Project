FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY src ./src
COPY web ./web

RUN mkdir -p out && find src -name "*.java" > sources.txt && javac -d out @sources.txt

EXPOSE 8080
CMD ["java", "-cp", "out", "Main"]
