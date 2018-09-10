Seena Rowhani
100945353
Option 1

Stack
 Backend
  Spark (jetty)
    sparkWebsockets
 Front End
  Ember
  https://github.com/srowhani/comp4004-a3-client

## Running Server

`java -jar out/artifacts/a3_jar/a3.jar`

## Running Development Client

`cd client && npm install && npm install -g ember-cli && ember s`

The way it's built currently, client dir builds directly into server's resources dist directory. Spark serves static files from dist.

