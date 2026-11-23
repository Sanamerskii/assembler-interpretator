#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
build_dir=$(mktemp -d)
trap 'rm -rf "$build_dir"' EXIT

javac --release 17 -d "$build_dir" "$project_dir"/src/*.java
java -cp "$build_dir" AssemblySource "$@"
