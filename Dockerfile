FROM gradle:6.2.2-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM openjdk:8-jre-slim
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/James_Bot-1.0-all.jar /app/app.jar
ENV botToken="ODUzOTE5MTQzMzMyODA2NjY3.YMcYLA._ldSj_JTy3lgPe4PS01xWazGWuY"
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
