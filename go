#!/bin/bash 

set -e

testall() {
  lein test
}

testunit() {
  lein test
}

push() {
  testall && git push
}

release() {
  lein release "$1"
}

serve() {
  lein run
}

if [ "$1" == "testall" ]; then
    testall
elif [ "$1" == "test" ]; then
    testunit
elif [ "$1" == "push" ]; then
    push
elif [ "$1" == "release" ]; then
    release "$2"
elif [ "$1" == "serve" ]; then
    serve
else
    echo "usage: $0 <goal>

goal:
    test     -- run unit tests
    testall  -- run all tests
    push     -- run all tests and push current state
    serve    -- run the sample pipeline"

    exit 1
fi
