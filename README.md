# probot

Profile Bot (aka **probot**) is a software package you can run to manage your social media accounts programatically.
 
**probot** can send direct messages to lists of followers, and reply to direct messages you receive.

## basics

**probot** back-end is implemented with [Scala](https://scala.io)
and packaged with [Apache Maven](http://maven.apache.org).

**probot** front-end is implemented with [Node.js](http://node.js)
and packaged with [Yarn](http://maven.apache.org).

## building

Run the following to build the back-end from source.

    mvn install
    
Run the following to build the front-end from source.

    yarn install

## preparation

Run the following to prepare the back-end for docker deployment.

    mvn clean package docker:build

Run the following to prepare the front-end for docker deployment.

    docker-compose build

You must ensure the following ports are not in use on your system, or modify docker-compose.yml and potentially other config files to change them:

  - 3000 (app)
  - 5000 (postgrest)
  - 5432 (postgres)
  - 8998 (livy)
  - 10000 (microservice)

## configuration

**probot** must be configured before launch with your social credentials.

Configuration file by default are expected in the working directory, same as the project root directory.

Alternative working directory or configuration file locations are possible with tweaks to docker-compose.yml.

Files you must edit:
  * docker-compose.yml
    Supply your own values for
    - services.ngrok.environment.NGROK_SUBDOMAIN
  * microservice.conf
    Supply your own values for
    - twitter.environment
    - twitter.oauth.*
    - server.hostname
  * collect-followers.conf
    Supply your own values for
    - TwitterFollowingConfiguration.info
    - TwitterFollowingConfiguration.max_pages

## running

Run the following to deploy the stack to local docker environment.

    export WORKDIR=`pwd`
    docker-compose up -d

## spot check

Check that basic twitter connectivity is established by inspecting your twitter webhook.

In a browser navigate to 'https://<subdomain>.ngrok.io/probot/twitter/webhook'

(Natually replace with your own configured ngrok subdomain.)

If everything is working you will see 'subscribed true' as final status.

## setup

Probot should launch with an active twitter account_activity connection, but a blank database.

Once basic connectivity is confirmed, the database must be backfilled manually to reflect existing followers.

This is a two-step process performed via shell (for now).

  1. Retrieve all followers programatically to followers.jsonl file.

      ``` docker exec -it livy java -cp /workdir/dist/probot-jar-with-dependencies.jar org.apache.streams.twitter.provider.TwitterFollowingProvider /workdir/collect-followers.conf /workdir/collect-followers.jsonl ```

  2. Load followers.jsonl to profiles table.

     ``` docker exec -it livy /usr/local/spark-2.4.6-bin-hadoop2.7/bin/spark-shell --packages org.postgresql:postgresql:9.4.1211 ```

     Copy the full contents of 'load-followers.scala' and paste it into session.

## dependencies

Probot back-end has the following essential library dependencies:

 * [Apache Juneau](http://juneau.incubator.apache.org "http://juneau.incubator.apache.org")
   HTTP microservice (based on Jetty) and data marshalling libraries
 * [Apache Spark](http://spark.apache.org "http://spark.apache.org")
   Data engineering / data science framework supporting batch and Stream processing
 * [Apache Streams](http://streams.apache.org "http://streams.apache.org")
   Twitter SDK, pojos, and connectivity

Probot front-end has the following essential dependencies:

 * [ReactJS](https://reactjs.org/ "https://reactjs.org/")
   A JavaScript library for building user interfaces
 * [React Admin](https://marmelab.com/react-admin/ "https://marmelab.com/react-admin/")
   A Web Framework for B2B applications
 * [Material UI](https://material-ui.com/ "https://material-ui.com/")
   React components for faster and easier web development.

Probot relies on several supporting run-time dependencies:

 * [Livy](https://livy.apache.org/ "https://livy.apache.org")
   A REST Service for Apache Spark
 * [Postgresql](https://www.postgresql.org/ "https://www.postgresql.org/")
   The World's Most Advanced Open Source Relational Database
 * [Postgrest](https://postgrest.org "http://www.postgrest.org")
   PostgREST is a standalone web server that turns your PostgreSQL database directly into a RESTful API.


