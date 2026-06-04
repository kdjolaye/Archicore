# ==========================
# STAGE 1 : BUILD
# On utilise l'image officielle Maven avec JDK 21 pour compiler
# ==========================
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copier uniquement les fichiers Maven en premier pour profiter du cache Docker :
# si le pom.xml n'a pas changé, cette couche est réutilisée sans re-télécharger les dépendances
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copier le code source et construire le jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==========================
# STAGE 2 : RUNTIME
# Image minimale JRE 21 (pas de JDK) → image finale plus légère et plus sécurisée
# ==========================
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Bonne pratique de sécurité : exécuter l'application avec un utilisateur non-root
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# Copier uniquement le jar du stage de build
COPY --from=builder /app/target/*.jar app.jar

# Créer le dossier de stockage et gérer les permissions
RUN mkdir -p /data/storage && chown -R appuser:appgroup /data/storage app.jar

USER appuser

EXPOSE 8080

# Options JVM pour environments containerisés :
# -XX:+UseContainerSupport : détecte automatiquement les limites mémoire du conteneur
# -XX:MaxRAMPercentage=75.0 : la JVM utilise au max 75% de la RAM allouée au conteneur
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]