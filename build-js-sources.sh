#!/bin/sh

mkdir -p resources

if [ ! -d "node_modules/ng-annotate" ]; then
    npm install ng-annotate
fi

if [ ! -d "node_modules/browserify" ]; then
    npm install browserify
fi

if [ ! -f "resources/ng-annotate.js" ]; then
    ./node_modules/.bin/browserify -r ng-annotate -o resources/ng-annotate.js
fi
