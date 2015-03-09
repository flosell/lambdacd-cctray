#!/bin/bash 

test() {
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

function serve() {
  lein run
}

if type $1 &>/dev/null; then
    $1 $2
else
    echo "usage: $0 <goal>

goal:
    test     -- run unit tests
    testall  -- run all tests
    push     -- run all tests and push current state
    serve    -- run the sample pipeline"

    exit 1
fi
