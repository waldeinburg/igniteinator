#!/usr/bin/env bash

set -e
cd "${0%/*}"

lein fig:build-sw
# Fix reference to window which is not accessible in a service worker.
# The must be some way to tell the Closure compiler to fix this.
sed -i -r 's/;(.+)=window;/;\1=self;/' target/public/sw.js
