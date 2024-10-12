---
theme: gaia
_class: lead
paginate: true
backgroundColor: #fff
backgroundImage: url('https://marp.app/assets/hero-background.svg')
---

# **Music metadata management system**

Http service for manage Artist, Tracks and Customer implemented in functional Scala

https://github.com/gilcu2/music_metadata

---

<!-- paginate: true -->
<!-- _class: toc -->

# Table of content

1. [Technology choices](#technology-choices)
2. [Further improvements](#further-improvements)

---

# Technology choices

- Given the task the main point is: Defining the HTTP server library
- Other libraries like for database management, configuration, logging, etc. are derived from compatibility with the main point
- Language and Libraries versions must be production ready because the target is about showing the skills for make a production ready system

---

## HTTP server library

- **http4s** was chosen because Typelevel stack was mentioned in the job description 
- The latest stable version warranties: 
  - strict type checking for input/output
  - high performance of many requests and resources deallocation (cats-effect based)
  - high performance of large requests
- zio-http was discarded because it is not Typelevel stack
- playframework or akka-http were discarded because they are non-functional

---

## Other libraries, platforms, external servers

- DB connection: doobie (typelevel, functional, resource secure)
- DB migrations: flyway because doobie compatible
- Serialization: circe (typelevel )
- Configurations: pureconfig (typelevel )
- DB: H2 (in memory for development ), for production an scalable SQL server like PostgreSQL
- Scala: 2.13 most stable and used
- Java: 21 Latest LTS

---

# Further improvements

Several direction for further improvements:

- Requirements: Given company purpose must be interesting to manage customer track relations like playing a song. Several endpoints can generate interesting results from this data
- Development: Improve code linting, testing, check coverage, versioning, changelog
- Deployment: Scalable DB server, Kubernetes deployment for scalability, logging and monitoring with professional tools 

---

## Further requirements

- Provide additional customer experiences:
  - Which tracks and how many times the customer have listened in some time interval
  - Which artist and how many times the customer have listened in some time interval
  - Which tracks and how many times all customers have listened in some time interval
  - Which artist and how many times all customers have listened in some time interval

---

## Improve development

- Linting: scalafix, sonarqube
- Testing: Integration tests again a production DB, scalability test
- Set minimum coverage 

---

## Improve deployment

- Scalable DB server (PostgreSQL, Redshift)
- Kubernetes deployment for server scalability (EKS)
- Centralized logging management, monitoring and alerts like Cloudwatch
