#!/bin/bash

id=$1

curl -H 'Content-Type: application/json' \
  http://localhost:8080/api/artist/$id