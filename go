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

if [ "$1" == "testall" ]; then
    testall
elif [ "$1" == "test" ]; then
    testunit
elif [ "$1" == "push" ]; then
    push
else
    echo "usage: $0 <goal>

goal:
    test     -- run unit tests
    testall  -- run all tests
    push     -- run all tests and push current state"
    exit 1
fi
