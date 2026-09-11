FROM tomcat:11.0-jdk21-temurin AS build
WORKDIR /workspace
COPY src ./src
RUN mkdir -p /workspace/classes && \
    find src -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 --release 21 \
      -cp "$CATALINA_HOME/lib/servlet-api.jar" -d /workspace/classes

FROM tomcat:11.0-jdk21-temurin
RUN rm -rf "$CATALINA_HOME/webapps"/* && mkdir -p "$CATALINA_HOME/webapps/ROOT/WEB-INF/classes"
COPY WebContent/ "$CATALINA_HOME/webapps/ROOT/"
COPY --from=build /workspace/classes/ "$CATALINA_HOME/webapps/ROOT/WEB-INF/classes/"
ENV PORT=10000
EXPOSE 10000
CMD ["sh", "-c", "sed -i -E 's/port=\"8080\"/port=\"'\"${PORT:-10000}\"'\"/' \"$CATALINA_HOME/conf/server.xml\" && exec catalina.sh run"]
