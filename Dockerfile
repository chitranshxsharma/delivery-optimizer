<<<<<<< HEAD
FROM node:18 AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

# ✅ ADD THIS LINE
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
=======
# Use Java 17 (or your version)
FROM eclipse-temurin:17-jdk-alpine

# Jar file path
ARG JAR_FILE=target/*.jar

# Copy jar
COPY ${JAR_FILE} app.jar

# Run app
ENTRYPOINT ["java","-jar","/app.jar"]
>>>>>>> 121b14eb9ea9086396e25e8dcb8d9e1e33f2a6c5
