#!/usr/bin/env bash
set -e

function heading() {
    echo -e "\033[0;30m\033[47m$1\033[0m"
}

heading "Fetching data ..."
./1-fetch-data.sh --images
heading "Verifying integrity ..."
./2-analyze-files.sh
heading "Downloading images ..."
./3-download-images.sh
heading "Converting images ..."
./4-convert-images.sh
heading "Generating data ..."
./5-generate-data.sh
