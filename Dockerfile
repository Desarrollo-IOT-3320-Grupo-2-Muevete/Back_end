# --- Etapa de Construcción ---
# Usamos una imagen de Maven con Java 17 para compilar el proyecto
FROM maven:3.8.5-openjdk-17 AS build

# Copiamos el código fuente a la imagen
COPY . .

# Ejecutamos el comando de construcción de Maven para crear el .jar
RUN mvn clean package -DskipTests

# --- Etapa de Ejecución ---
# Usamos una imagen ligera de solo Java 17 para correr la aplicación
FROM eclipse-temurin:17-jre-jammy

# Copiamos el archivo .jar que se creó en la etapa anterior
COPY --from=build /target/api-0.0.1-SNAPSHOT.jar app.jar

# Le decimos a Docker que el puerto 8082 debe estar disponible
EXPOSE 8082

# El comando para iniciar la aplicación cuando el contenedor se ejecute
ENTRYPOINT ["java", "-jar", "app.jar"]