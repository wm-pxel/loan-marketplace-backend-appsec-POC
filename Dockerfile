#Dependencies container
FROM gradle:8.11.1-jdk17-alpine AS dependencies
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle .

#Download dependencies
RUN gradle clean build -i --stacktrace

#Main container
FROM gradle:8.11.1-jdk17-alpine AS builder
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
WORKDIR /app

#Copy cache from dependencies container
COPY --from=dependencies /home/gradle /home/gradle/

COPY . .

RUN gradle bootJar -i --stacktrace

FROM gradle:8.11.1-jdk17-alpine

COPY --from=builder /app/build/libs/*.jar /app/app.jar

WORKDIR /app

EXPOSE 10800

CMD ["java", "-jar", "app.jar"]