#! /usr/bin/env bash

gradle bootJar -i --stacktrace
cd build/libs
java -jar loan-syndication-0.0.1-SNAPSHOT.jar