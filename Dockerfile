FROM eclipse-temurin:17-jre
RUN mkdir /opt/cook
COPY dist/ /opt/cook
WORKDIR /opt/cook
ENTRYPOINT java --add-opens java.base/java.util.concurrent=ALL-UNNAMED -jar cook.jar 

