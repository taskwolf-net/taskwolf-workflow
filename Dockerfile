FROM openjdk:21

COPY /build/libs/workflow-1.0.0-SNAPSHOT.jar workflow.jar
COPY /locale/ /locale/