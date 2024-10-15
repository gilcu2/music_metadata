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
- Programming language: Scala is a requirement
- Versions: Production ready
- Persistence and data processing
- HTTP server library
- Other libraries
- Deployment

---

## Persistence and data processing

An SQL relational database is the best match:
- Well and strict defines entities (Artist, Track, Customer) in 1-n relations 
- SQL relational databases is the most mature DB technology
- Easiest and more efficient way to implement the requirements
- H2 memory for development
- PostgreSQL for production
- CockroachDB in case more scalability needed 


--- 

## HTTP server library

- **http4s** was chosen because:
- Is part of Typelevel stack
- Stable version warranties: 
  - strict type checking for input/output
  - secure
  - high performance of many requests and resources deallocation (cats-effect based)
  - high performance of large requests (fs2 based)
- Discarded: zio-http (not Typelevel stack), playframework or akka-http (non-functional)

---

## Other libraries, platforms, external servers

- DB connection: doobie (typelevel, functional, resource secure)
- DB migrations: flyway because doobie compatible
- Serialization: circe (typelevel )
- Configurations: pureconfig (typelevel )
- DB: H2 (in memory for development ), for production a scalable SQL server like PostgreSQL
- Scala: 2.13 most stable and used
- Java: 21 Latest LTS

---

# Further improvements

Several direction for further improvements:

- Requirements: Manage customer track relation and related services
- Development: Improve code linting, testing, test coverage, changelog, versioning, releasing
- Security: SSL, Authentication, permissions, CSRF
- Deployment: Scalability, Customer trust, logging, monitoring and alerts

---

## Requirements

- Provide additional experiences:
  - Which tracks and how many times the customer have listened in some time interval
  - Which artist and how many times the customer have listened in some time interval
  - Which tracks and how many times all customers have listened in some time interval
  - Which artist and how many times all customers have listened in some time interval
- OpenAPI description (tapir) 

---

## Improve development

- Refine types for data validation
- Linting: scalafix, sonarqube
- Testing: Integration tests again a production DB, scalability test
- Set minimum test code coverage and improve coverage
- Implement a CHANGELOG file that keep track of all changes to the project
- Versioning: Track the development states (semantic versioning)
- Releasing: Track published states

---

# Security

- SSL: Encrypt communication client-server
- Authentication and permission: Allows to set the right to execute each service
- CSRF: Protect against client program execute unwanted user actions


---

## Improve deployment

- Scalable DB server (PostgreSQL, Redshift)
- Domain name for app (Customer trust)
- Kubernetes deployment:
  - App scalability through load balancer and autoscaling pods
  - SSL with authorized certificate (Customer trust)
- Centralized logging management, monitoring and alerts like Cloudwatch
