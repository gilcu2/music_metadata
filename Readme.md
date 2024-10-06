# Music metadata service for music streaming platform

The service provide functionality for management the entities related to a music streaming service like artist, 
music tracks and customer.

## Architecture

The service was implemented four layer:
- Model: Define the entities and its data. Was implemented using Scala case classes
- Repository: Define the data persistence and functionality implemented at the database level. 
  Implemented using Doobie and Flyway libraries. It allows to connect to any JDBC datasource and 
  database evolution trough migrations.
- Http Api: Provide the endpoints for the connection with the service. Was implemented using the Http4s library
- Server: Execute the endpoints on a http service. Was implemented using BlazeServer
  
All layers were implemented as effects based on the cats-effect library. In this way, running the service
make use of the IOAPP.
Besides other libraries were used for configuration, logging and serialization/deserialization.

## Requirements
 
- sbt
- The libraries versions defined require at least Java 21.

## Testing

Asynchronous test were implemented on Scalatest using cats--effect support library AsyncIOSpec. 
The four layer were tested. For execute the tests run in project directory:

```shell
sbt test
```

## Running

For execute the server run in project directory:

```shell
sbt run
```

In a browser use the link http://localhost:8080/api/hello/ice

For more complex action like create an artist another tools like curl are needed. 
The tests in src/test/scala/ like ServerSpec.scala, RouterSpec.scala show what have 
to be done for use the service.

## Further steps to production
- Implement changelog and version management
- Implement continuous integration using tools like Github actions
- Implement integration tests using databases with persistence and scalability like PostgreSQL
  Use tools like docker compose for running the test. Test scalability
- Implement deployment using kubernetes for scalability and use a scalable database