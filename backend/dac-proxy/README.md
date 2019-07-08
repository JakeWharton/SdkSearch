DAC Proxy
=========

A proxy which sits between DAC and the apps allowing normalization of its content to a standard JSON
format and insulating the apps from changes to DAC.


Deploy
------

Currently deployed to Heroku.


#### One time setup:

Install the Heroku CLI, authenticate, and configure the git remote.

Ensure the config uses configuration-on-demand and only builds this module:

    $ heroku domains:add api.sdksearch.app
    $ heroku config:set GRADLE_TASK="--configure-on-demand -Dkotlin.incremental=false :backend:dac-proxy:installDist"


#### Release

Ensure functional tests pass:

    $ ./gradlew :backend:dac-proxy:functionalTest

Commit to master and then push to heroku:

    $ git push heroku master
