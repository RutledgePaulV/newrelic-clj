#!/usr/bin/env bash

clj -X:build clean
clj -X:build jar

export CLOJARS_USERNAME="op://Personal/clojars.org/username"
export CLOJARS_PASSWORD="op://Personal/clojars.org/token"

op run -- mvn deploy:deploy-file \
  -DgroupId="io.github.rutledgepaulv" \
  -DartifactId="newrelic-clj" \
  -Dversion="$(clj -X:build get-version)" \
  -Dpackaging="jar" \
  -Dfile="target/newrelic-clj.jar" \
  -DrepositoryId="clojars" \
  -Durl="https://repo.clojars.org"