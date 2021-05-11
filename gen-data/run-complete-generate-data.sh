#!/usr/bin/env bash
set -e

function heading() {
    echo -e "\033[0;30m\033[47m$1\033[0m"
}

heading "Fetching data ..."
./1-fetch-data.sh
heading "Verifying integrity ..."
./2-analyze-files.sh
heading "Generating data ..."
./3-generate-data.sh
