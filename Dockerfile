FROM openjdk:17.0.1-jdk-slim

RUN apt-get -y update
RUN apt -y install wget
RUN apt -y install unzip
RUN apt -y install curl
RUN apt -y install gnupg
RUN apt -y install ca-certificates

# google chrome 설치
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
RUN apt-get -y update
RUN apt -y install ./google-chrome-stable_current_amd64.deb
RUN rm google-chrome-stable_current_amd64.deb

ENV CHROME_BIN=/usr/bin/google-chrome
ENV TZ=Asia/Seoul

ARG JAR_FILE=build/libs/earth_talk-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]