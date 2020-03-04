FROM openjdk:11-jdk-slim
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew build
CMD ./gradlew run

