#!/bin/bash

echo Building backend
cd api
#clj -T:build all > /dev/null
cd ..

echo Building frontend
cd webapp
#npx shadow-cljs release :frontend > /dev/null
cd ..

echo Copying to dist folder
mkdir -p dist/webapp/js
cp api/target/cook*.jar dist/cook.jar
cp webapp/public/index.html dist/webapp/index.html
cp webapp/public/js/main.js dist/webapp/js/main.js
scp -r -P2223 dist/* 192.168.10.100:/opt/cook

