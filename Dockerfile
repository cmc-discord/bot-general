FROM openjdk:17-jdk-slim

COPY build/libs/*-all.jar /usr/local/lib/bot.jar

RUN mkdir /bot
RUN mkdir /bot/plugins
RUN mkdir /bot/data

WORKDIR /bot

ENTRYPOINT ["java", "-Xms512M", "-Xmx512M", "-jar", "/usr/local/lib/bot.jar"]
