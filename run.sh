#!/bin/sh
set -eu

case "${1:-}" in
    assemble) main_class=Assembler ;;
    interpret) main_class=Interpreter ;;
    *)
        printf '%s\n' 'Использование: ./run.sh {assemble|interpret} аргументы' >&2
        exit 1
        ;;
esac
shift

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
build_dir=$(mktemp -d)
trap 'rm -rf "$build_dir"' EXIT

javac --release 17 -d "$build_dir" "$project_dir"/src/*.java
java -cp "$build_dir" "$main_class" "$@"
