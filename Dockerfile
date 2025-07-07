FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y ffmpeg curl

ADD target/*.jar live-stream-be-website.jar

ENTRYPOINT ["java","-jar","/live-stream-be-website.jar"]

EXPOSE 8081