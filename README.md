# Igniteinator

Source code for the [Igniteinator](https://igniteinator.waldeinburg.dk) – an unoffical app for the
board game [Ignite](http://gingersnapgaming.com).

## Overview

The Igniteinator is a fan-made app for Ignite, a deck builder board game. The code could be
abstracted and made into a library (which I shall call Deckbuildinator) but for now it's all about
Ignite.

The aim of the project is to implement all features of the official app, just better, and to add a
deck randomizer mechanism.

## Development setup

### Remote sync script

If deploying: Symlink in `igniteinator` folder to `remote_sync.sh` from
[Poor mans rsync](https://github.com/waldeinburg/poor-mans-rsync).

### Development certificate

Run:

    lein run -m certifiable.main

Then, in `igniteinator` folder, symlink to the generated file referenced in `main.cljs.edn`:

    ln -s /home/.../_certifiable_certs/.../dev-server.jks dev-keystore.jks

In Chrome: Go to [Settings - Manage certificates](chrome://settings/certificates), choose
`Authorities`, then click `Import` and
choose `/home/.../_certifiable_certs/.../dev-root-trust-this.pem`.

Forget adding the certificate in Android; it doesn't work.

### Generate data

To download images and generate data files:

    cd gen-data && ./run-complete-generate-data.sh

## Development

The following applies to the `igniteinator` folder:

To get an interactive development environment with [Figwheel](https://figwheel.org):

    lein fig:dev

To clean all compiled files:

	lein clean

To test out doing a production build:

	./build-and-deploy.sh --no-tag --no-deploy

## Licenses

### Data and images

All data and images are Copyright © 2021, [Ginger Snap Gaming](http://gingersnapgaming.com). Used in
the [Igniteinator](https://igniteinator.waldeinburg.dk) by permission.

Thus, even though the scripts in the code fetches data and images, forking the project does not give
you permission to publish another version of the app. You will need to get permission
from [Ginger Snap Gaming](http://gingersnapgaming.com).

### Source code

Copyright © 2021 Daniel Lundsgaard Skovenborg

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later
version.