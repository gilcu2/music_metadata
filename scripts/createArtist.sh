#!/bin/bash

name=$1

curl -H 'Content-Type: application/json' \
           -d '{ "name":"'$name'"}' \
           -X POST \
           http://localhost:8080/api/artist

