#!/bin/bash

echo Building backend
cd api
clj -T:build all &> /dev/null &
cd ..

echo Building frontend
cd webapp
npm install &> /dev/null && npx shadow-cljs release :frontend &> /dev/null &
cd ..

wait

echo Copying to dist folder
mkdir -p dist/webapp/js
cp api/target/cook*.jar dist/cook.jar
cp webapp/public/index.html dist/webapp/index.html
cp webapp/public/js/main.js dist/webapp/js/main.js

docker build . -t registry.neksa.no/cook:$1
docker push registry.neksa.no/cook:$1
