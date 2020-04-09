FROM gradle:jdk14 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle distTar

FROM openjdk:14-alpine

ARG VERSION=1.0

WORKDIR /app
COPY --from=builder /home/gradle/src/build/distributions/mlab-data-api-$VERSION.tar /app/
RUN tar -xvf mlab-data-api-$VERSION.tar
WORKDIR mlab-data-api-$VERSION
CMD bin/mlab-data-api
