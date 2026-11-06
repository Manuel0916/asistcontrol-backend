# ============================
# Etapa 1: Construcción
# ============================
FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app

# Copiar solo Maven wrapper y config para cache
COPY mvnw pom.xml ./
COPY .mvn .mvn
# mvnw puede venir con saltos de linea de Windows (CRLF): se corrigen para Linux
RUN sed -i 's/$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline --batch-mode

# Copiar código fuente
COPY src src

# Construir JAR (sin tests, batch-mode)
RUN ./mvnw clean package -DskipTests --batch-mode

# ============================
# Etapa 2: Producción ligera
# ============================
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Copiar JAR generado
COPY --from=builder /app/target/*.jar app.jar

# Puerto Spring Boot
EXPOSE 8080

# Comando de inicio. Limites de memoria pensados para planes gratis (512 MB de RAM)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]