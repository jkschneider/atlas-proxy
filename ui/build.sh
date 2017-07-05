#!/bin/bash

yarn install
yarn build
cp -r build ../service/src/main/resources/static
echo 'Static resources copied to ../service/src/main/resources/static'