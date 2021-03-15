FROM adoptopenjdk:15-jdk AS builder
RUN apt-get update && apt-get install maven binutils -y
WORKDIR /workdir
ADD pom.xml pom.xml
RUN mvn dependency:go-offline
ADD lombok.config lombok.config
ADD src/ src/
RUN mvn package -DskipTests

FROM adoptopenjdk:15-jre
WORKDIR /mvnmon
COPY --from=builder /workdir/target/lib/ /mvnmon/lib/
COPY --from=builder /workdir/target/mvnmon*.jar /mvnmon/mvnmon.jar
ENTRYPOINT ["java", "--enable-preview", "-cp", "/mvnmon/lib/*:mvnmon.jar", "dev.mck.mvnmon.MvnMonApplication"]
