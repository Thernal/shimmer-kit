#!/bin/sh
# Builds the kit once with each feature of kit.yml left out — what an app that does not take it gets.
# A feature's code must be referenced by nothing outside it (knowledge docs/KITS.md); this is where a
# reference that crept in fails, instead of in the first app that leaves the feature out.
#
#   scripts/check-features.sh            every feature, one build each
#   scripts/check-features.sh glow       only those named
set -eu
cd "$(dirname "$0")/.."
features=${*:-$(awk '/^feature:[[:space:]]*$/ {f=1; next} f && /^[[:space:]]*-[[:space:]]/ {print $2; next} f && !/^[[:space:]]*(#.*)?$/ {f=0}' kit.yml)}
for feature in $features; do
    printf '== without %s\n' "$feature"
    ./gradlew build -Pkit.without="$feature" --quiet
done
printf '== without all of them\n'
./gradlew build -Pkit.without="$(printf '%s' "$features" | tr ' \n' ',,')" --quiet
printf 'every feature can be left out\n'
