FROM openjdk:21

ADD target/*.jar live-stream-be-website.jar

ENTRYPOINT ["java","-jar","/live-stream-be-website.jar"]

EXPOSE 8081