#!/bin/sh

mkdir -p resources

if [ ! -d "node_modules/ngmin" ]; then
    npm install ngmin
fi

if [ ! -d "node_modules/browserify" ]; then
    npm install browserify
fi

if [ ! -f "resources/ngmin.js" ]; then
    ./node_modules/.bin/browserify -r ngmin -o resources/ngmin.js
fi
