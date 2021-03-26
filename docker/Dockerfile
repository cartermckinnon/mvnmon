FROM adoptopenjdk/openjdk16 AS builder
ARG DL_URL=https://apache.osuosl.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
ARG SHA=c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0
RUN mkdir -p /usr/share/maven \
    && curl -Lso  /tmp/maven.tar.gz ${DL_URL} \
    && echo "${SHA}  /tmp/maven.tar.gz" | sha512sum -c - \
    && tar -xzC /usr/share/maven --strip-components=1 -f /tmp/maven.tar.gz \
    && rm -v /tmp/maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

WORKDIR /workdir
ADD pom.xml pom.xml
RUN mvn dependency:go-offline -q
ADD src/ src/
RUN mvn package -DskipTests -q

FROM adoptopenjdk/openjdk16:jre
WORKDIR /mvnmon
COPY --from=builder /workdir/target/lib/ /mvnmon/lib/
COPY --from=builder /workdir/target/mvnmon*.jar /mvnmon/mvnmon.jar
ENTRYPOINT ["java", "--enable-preview", "-cp", "/mvnmon/lib/*:mvnmon.jar", "dev.mck.mvnmon.MvnMonApplication"]
ADD configuration/db.yaml configuration.yaml