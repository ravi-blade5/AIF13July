# Traceability: MFG-ARC-PAY-CORE-001 v1.0 / Payment Engine / AIF-169..AIF-174
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY target/payment-engine-*.jar /app/payment-engine.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseZGC", "-jar", "/app/payment-engine.jar"]
