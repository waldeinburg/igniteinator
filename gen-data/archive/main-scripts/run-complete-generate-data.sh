#!/usr/bin/env bash
set -e
cd "${0%/*}"

function heading() {
    echo -e "\033[0;30m\033[47m$1\033[0m"
}

heading "Fetching data ..."
./1-fetch-data.sh --images
heading "Verifying integrity ..."
./2-analyze-files.sh
heading "Converting images ..."
./3-convert-images.sh
heading "Generating data ..."
./4-generate-data.sh
