# http4s + doobie + docker scratchpad [![Build Status](https://travis-ci.org/lloydmeta/http4s-doobie-docker-scratchpad.svg?branch=master)](https://travis-ci.org/lloydmeta/http4s-doobie-docker-scratchpad)

My little sandbox for playing around with http4s, doobie, docker, testing, etc.

Explores:

* Using [http4s](http4s.org) with cats-effect
* Using [doobie](http://tpolecat.github.io/doobie/docs/01-Introduction.html) with cats-effect
* [Pureconfig](https://github.com/pureconfig/pureconfig) for loading configuration
* [Flyway](https://flywaydb.org/) for handling migrations
* []Docker testkit](https://github.com/whisklabs/docker-it-scala) for bringing up Docker containers on a per-test-suite basis
* [sbt-native-packager](sbt-native-packager.readthedocs.io) for bundling the app into a Docker image
* [sbt-docker-compose](https://github.com/Tapad/sbt-docker-compose) for bringing up a docker-compose environment based on the main project and a docker-compose.yml file
    * Can run the whole project from sbt via `sbt dockerComposeUp` (stopped via `dockerComposeStop`)
    * Also used for running Integration tests from a separate subproject

## Usage

[Docker](https://www.docker.com/) is essential to having fun here, so go download it if you don't have it installed

### Tweets API Swagger

`sbt dockerComposeUp` and go to the root in your browser (e.g. [localhost](http://localhost)).

### Tests

* `sbt test` will run normal tests
* `sbt dockerComposeTest` will run integration tests
* `sbt dockerComposeUp` will start a docker-compose environment that includes the web-server and dependencies.

## Feedback

Help me learn ! Submit issues and PRs :)