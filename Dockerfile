FROM openjdk:11-jdk-slim
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew build
EXPOSE 8080
CMD ./gradlew run

