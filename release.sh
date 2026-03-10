#!/usr/bin/env bash
set -e

clj -X:build clean
clj -X:build jar
version=$(clj -X:build get-version)
export OP_ACCOUNT="my.1password.com"

op run --env-file="./.env" -- mvn deploy:deploy-file \
  -DgroupId="io.github.rutledgepaulv" \
  -DartifactId="newrelic-clj" \
  -Dversion="$version" \
  -Dpackaging="jar" \
  -Dfile="target/newrelic-clj.jar" \
  -DrepositoryId="clojars" \
  -Durl="https://repo.clojars.org"

git tag "v$version"
git push origin "refs/tags/v$version"
