# Title Manager Challenge

## Problem Statement
Implement and test in any language a working RESTful API to create, retrieve, update and delete Walt Disney Studios titles metadata for Feature, TV and Bonus content. For the retrieve operation, include capabilities to filter by Title Type (Bonus, Episode, Season, TV Series, and Feature).
Use the following diagram and the provided JSON file with bulk data for reference:

![class hierarchy](https://raw.githubusercontent.com/ulisesbocchio/titles-reference/master/hierarchy.png)

This diagram and the JSON file are for guidance purposes only. It is not required to implement exactly the above class hierarchy.

## Deliverables
A zip/rar/tar file (or public github/gitlab repo URL) that contains the source code and a README file with instructions on how to Build, Test and Run the Application and a description of the implemented endpoints and how they are used.

## Metrics (For Reviewers)
- Setup/bootstrap
- Testing
- Endpoint organization/pattern
- Relationship representation/persistence/validation
- Relationship retrieval (how season/episodic/bonus is represented in responses and what level)

# Implementation

## Tech Stack

- Kotlin
- Spring Boot 2.0
- Spring Webflux
- MongoDB
- Docker

## Test, Build and Run

###  In one step
```bash
./gradlew build docker composeUp
```

### Build only
Build JAR and Docker Image
````bash
./gradlew build docker
````

### Test only
Run Spring Integration Tests
````bash
./gradlew test
````

### Start in container
Run the app in a docker-compose environment
```bash
./gradlew composeUp

``` 
### Start standalon 
Running the app standalone requires MongoDB running on localhost:27017
````bash
./gradlew bootRun
````

## API Reference

### GET /titles
Returns all titles. Response includes plain Title metadata (no parent or child titles).

This endpoint accepts query params:
- `type`: comma-separate Title types to filter results (`Bonus`, `Feature`, `TV Series`, `Season`, or `Episode` are valid options)
- `terms`: word(s) or phrase to filter titles by. Filter applies to Title name only.

Sample Response:
```json
[
  {
    "type": "Bonus",
    "id": "5a8913d6857aba00017d00dc",
    "name": "Breaking the Ice",
    "description": "Get to know frozen from the snowy ground up as the filmmakers and songwriters discuss the story's roots and inspiration; the joys of animating olaf, the little snowman with the sunny personality; and the creation of those amazing songs.",
    "duration": "15 min"
  },
  {
    "type": "Bonus",
    "id": "5a8913d6857aba00017d00dd",
    "name": "Deleted Scene: Meet Kristoff 2 - Introduction By Directors",
    "description": "Kristoff goes mountain climbing with a friend. With an introduction by directors chris buck and jennifer lee.",
    "duration": "13 min"
  }
]
```

### POST /titles
Creates a Title. Body Sample:

```json
{
  "type": "Bonus",
  "name": "Breaking the Ice",
  "description": "Get to know frozen from the...",
  "duration": "15 min"
}
```
Where `type` is required.

### GET /titles/{id}
Returns a title by ID. Response includes parent and child Titles.

Sample Response:

```json
{
  "type": "TV Series",
  "id": "5a8918e5a2ed17523ec6d527",
  "name": "Lost",
  "description": "A plane crashes on a Pacific island, and the 48 survivors, stripped of everything, scavenge what they can from the plane for their survival. Some panic; some pin their hopes on rescue. The band of friends, family, enemies, and strangers must work together against the cruel weather and harsh terrain.",
  "releaseDate": "2004-09-22T00:00:00.000+0000",
  "seasons": [
    {
      "type": "Season",
      "id": "5a8918e5a2ed17523ec6d528",
      "name": "Season 1",
      "episodes": [
        {
          "type": "Episode",
          "id": "5a8918e5a2ed17523ec6d529",
          "name": "...In Translation",
          "releaseDate": "2004-09-22T00:00:00.000+0000",
          "duration": "42 min"
        },
        {
          "type": "Episode",
          "id": "5a8918e5a2ed17523ec6d52a",
          "name": "All the Best Cowboys Have Daddy Issues",
          "releaseDate": "2004-09-29T00:00:00.000+0000",
          "duration": "42 min"
        }
      ]
    },
    {
      "type": "Season",
      "id": "5a8918e5a2ed17523ec6d52c",
      "name": "Season 2",
      "episodes": [
        {
          "type": "Episode",
          "id": "5a8918e5a2ed17523ec6d52d",
          "name": "...And Found",
          "releaseDate": "2005-09-21T00:00:00.000+0000",
          "duration": "42 min"
        }
      ]
    }
  ]
}
```

### PUT /titles/{id}
Updates a Title by ID.

Body Sample:

```json
{
  "type": "Bonus",
  "name": "Breaking the Ice",
  "description": "Get to know frozen from the...",
  "duration": "15 min"
}
```
Where `type` is required.

### DELETE /titles/{id}
Deletes a Title.

For instance:
```
DELETE /titles/123
```
Would remove Title `321`

### PUT /titles/{id}/{childType}/{childId}
Adds a child of a given type to a given Title.

For instance:
```
PUT /titles/123/episodes/321
```
Would add Episode `321` to TV Series `123`


### DELETE /titles/{id}/{childType}/{childId}
Removes a child of a given type to a given Title.

For instance:
```
DELETE /titles/123/episodes/321
```
Would remove Episode `321` from TV Series `123`
