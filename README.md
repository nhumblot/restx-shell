# RESTX SHELL - Command Line Interface for RestX projects

[![Build Status](https://travis-ci.org/restx/restx-shell.svg?branch=master)](https://travis-ci.org/restx/restx-shell)

RESTX Shell is a command line interface (CLI) for managing restx projects, from bootstrapping to daily usages like adding a dependency.

It's licensed under the very commercial friendly Apache License 2, and is actively maintained by a community of developers.

You can get more details about RESTX from the web site at http://restx.io/

Here you will find the build instructions if you want to build RESTX Shell yourself, and why not contribute to the project.

## Build

RESTX Shell requires Java 7.

You can build it using either Maven 2+ or EasyAnt.

With Maven:

`mvn install`

With EasyAnt:

`easyant test package`

To test current version of the shell, build it then :

- Move to `restx-package` directory
- Run the `local-install.sh` script which will install a development `restx-shell` distribution into your `restx-package/target/restx/` directory.
- Create a `RESTX_OPTS` environment variable with debug properties, such as :
  ```
  export RESTX_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8001"
  ```
  and run the `restx-package/target/restx/restx` executable : you will then be able to debug a running shell.

## Changing dependencies

The sources for module descriptors are the `md.restx.json` files located in each module, and the `restx.build.properties` for the dependencies version.

Maven poms and EasyAnt Ivy files are generated from these files using `restx build generate pom + build generate ivy`.

## Project organisation

RESTX Shell is decomposed in a set of modules, each one following the traditional Java project layout (main sources in `src/main`, test sources in `src/test`).

The main modules are `restx-shell`, `restx-build` and `restx-shell-manager`.

RESTX Shell is a plugin-oriented shell with currently 3 main plugins : `restx-core-shell`, `restx-build-shell` and `restx-specs-shell`.

Here is a brief summary of each module:

- `restx-shell`: The pluggable shell.
- `restx-shell-manager`: Plugin to manage the shell: install plugins, upgrade shell version.
- `restx-build`: The very simple tool which generates POM / Ivy files from `md.restx.json` files
- `restx-core-shell`: Plugin providing RESTX core support in the shell: app comilation and running, ...
- `restx-build-shell`: Plugin prodividing build support in the shell, especially to generate POM/Ivy files from `md.restx.json` files.
- `restx-specs-shell`: Plugin providing RESTX specs based HTTP mock server.
- `restx-package`: assembly module to package the shell

## Contributing

Contributions are welcome, fork the repo, push your changes to a branch, build & debug it then send a Pull Request.

To be sure the PR will be merged please discuss it on the google group before, or create an issue on GitHub to initiate the discussion.
