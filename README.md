# titles-reference
Reactive Spring Boot 2.0 Kotlin Reference Title API

## Build
Build JAR and Docker Image
````bash
./gradlew build docker
````

## Run
Run the app in a docker-compose environment
```bash
docker-compose up
``` 

## API Reference

### GET /titles/{id}

Returns a title by ID

### GET /titles

Returns all titles