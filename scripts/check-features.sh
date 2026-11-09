#!/bin/sh
# Builds the kit once with each feature of kit.yml left out — what an app that does not take it gets.
# A feature's code must be referenced by nothing outside it (knowledge docs/KITS.md); this is where a
# reference that crept in fails, instead of in the first app that leaves the feature out.
#
# A feature here is a directory inside `impl`, so it is moved aside for the build and put back after.
# The sample names every effect and is never copied, so only the modules an app copies are built.
#
#   scripts/check-features.sh            every feature, one build each
#   scripts/check-features.sh glow       only those named
set -eu
cd "$(dirname "$0")/.."

feature_list() { awk '/^feature:[[:space:]]*$/ {f=1; next} f && /^[[:space:]]*-[[:space:]]/ {print $2, $3; next} f && !/^[[:space:]]*(#.*)?$/ {f=0}' kit.yml; }
features=${*:-$(feature_list | awk '{print $1}')}
targets=$(awk '/^code:[[:space:]]*$/ {f=1; next} f && /^[[:space:]]*-[[:space:]]/ {gsub("/", ":", $2); print ":" $2 ":build"; next} f && !/^[[:space:]]*(#.*)?$/ {f=0}' kit.yml)

parked=$(mktemp -d)
restore() {
    [ -s "$parked/.moved" ] || return 0
    while IFS= read -r path; do
        mv "$parked/$path" "$path"
    done <"$parked/.moved"
    : >"$parked/.moved"
}
trap 'restore; rm -rf "$parked"' EXIT
trap 'exit 130' INT TERM

build_without() {
    for name in "$@"; do
        for path in $(feature_list | awk -v n="$name" '$1 == n {print $2}' | tr ',' ' '); do
            mkdir -p "$parked/$(dirname "$path")"
            mv "$path" "$parked/$path"
            printf '%s\n' "$path" >>"$parked/.moved"
        done
    done
    # shellcheck disable=SC2086 # one word per Gradle task
    ./gradlew $targets --quiet
    restore
}

for feature in $features; do
    printf '== without %s\n' "$feature"
    build_without "$feature"
done
printf '== without all of them\n'
# shellcheck disable=SC2086 # one word per feature
build_without $features
printf 'every feature can be left out\n'
