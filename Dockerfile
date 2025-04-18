# cd standalone/loader/build/libs
#
# cp MoonRise-*.jar moonrise-standalone.jar
# docker build . -t moonrise -f ../../../../Dockerfile

FROM eclipse-temurin:21-alpine
RUN apk add --no-cache netcat-openbsd

# create a simple 'send' command that will allow users
# to run, for example: docker exec <container> send m info
RUN printf '#!/bin/sh\n\
echo "$@" | nc -NU /opt/moonrise/moonrise.sock\n' >> /usr/bin/send && chmod 777 /usr/bin/send

# setup user
RUN addgroup -S app && adduser -S -G app app
USER app

# copy jar file into image
WORKDIR /opt/moonrise
COPY moonrise-standalone.jar .

# preload and relocate dependency jars
RUN java -jar moonrise-standalone.jar preloadDependencies

CMD ["java", "-jar", "moonrise-standalone.jar", "--docker"]

HEALTHCHECK --interval=30s --timeout=15s --start-period=20s \
    CMD wget http://localhost:3001/health -q -O - | grep -c '"isHealthy":true' || exit 1