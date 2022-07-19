# Igniteinator

Source code for the [Igniteinator](https://igniteinator.waldeinburg.dk) – an unoffical app for the
board game [Ignite](https://gingersnapgaming.com).

## Overview

The Igniteinator is a fan-made app for Ignite, a deck builder board game. The code could be
abstracted and made into a library (which I shall call Deckbuildinator) but for now it's all about
Ignite.

The data are originally based on a now non-existent official app (cf. the `gen-data/archive` source
folder for how the data was processed and developed). Images have been kindly provided by [Ginger
Snap Gaming](https://gingersnapgaming.com).

## Development setup

_It is not possible to set up a development environment from scratch because the images are not
included in the repository. If you want to contribute, please contact me and I will create scripts
for downloading the data from the currently deployed Igniteinator._

### Install npm packages

Run once to install npm packages:

    npm install

### Remote sync script

If deploying: Symlink to `remote_sync.sh` from
[Poor mans rsync](https://github.com/waldeinburg/poor-mans-rsync).

### Development certificate

To ensure that the service worker is functioning without bypassing security a certificate is
generated and registered. The process needs to be repeated after a couple of months (the symptom
is that Chrome complains that the server cannot be reached).

Run:

    lein run -m certifiable.main

Then symlink to the generated file referenced in `main.cljs.edn`:

    ln -s /home/.../_certifiable_certs/.../dev-server.jks dev-keystore.jks

In Chrome: Go to [Settings - Manage certificates](chrome://settings/certificates), choose
`Authorities`, then click `Import` and
choose `/home/.../_certifiable_certs/.../dev-root-trust-this.pem`.

Forget adding the certificate in Android; it doesn't work.

## Development

To get an interactive development environment with [Figwheel](https://figwheel.org):

    lein fig:dev

To clean all compiled files:

	lein clean

To test out doing a production build:

	./build-and-deploy.sh --no-tag --no-deploy

## Licenses

### Data and images

All data and images are Copyright © 2021, [Ginger Snap Gaming](https://gingersnapgaming.com). Used
in the [Igniteinator](https://igniteinator.waldeinburg.dk) by permission.

Thus, forking the project does not give you permission to publish another version of the app. You
will need to get permission from [Ginger Snap Gaming](http://gingersnapgaming.com).

### Source code

Copyright © 2021 Daniel Lundsgaard Skovenborg

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later
version.
