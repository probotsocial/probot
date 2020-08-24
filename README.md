# probot

Profile Bot (aka **probot**) is a software package you can run to help manage your social media accounts.

**probot** collects, organizes, and presents all data available to you via apis, exports, and other sources.
 
**probot** is proactive; it can be configured to:
  - auto-follow accounts that follow you
  - auto-reply to direct messages you receive
  - send direct messages to specific subsets of followers
  - auto-block trolls who reply to your content
  - and more.

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

For more information on setting up a development environment:
[dev-setup.md](./dev-setup.md)

## preparation

Run the following to prepare for docker deployment.

    docker-compose build

You must ensure the following ports are not in use on your system, or modify docker-compose.yml and potentially other config files to change them:

  - 3000 (app)
  - 5000 (postgrest)
  - 5432 (postgres)
  - 6123 (jobmanager)
  - 8081 (jobmanager)
  - 8890 (zeppelin)
  - 10000 (microservice)

## configuration

**probot** must be configured before launch with your social credentials.

Configuration you must edit:
  * src/main/resources/conf/twitter.conf
    - twitter.environment
    - twitter.oauth.*
    - server.hostname

## running

Run the following to deploy the stack to local docker environment.

    docker-compose up -d

## initial data acquisition

The database must be populated manually.

This is a multi-step process managed via zeppelin.

For more details:
[twitter-setup.md](./twitter-setup.md)

## connectivity

Probot supports an active real-time twitter account_activity connection.

You will not be able to establish real-time connectivity with twitter unless you 
expose microservice to the internet with a high-quality SSL configuration.

How to do that is beyond the scope of this document.

## spot check

Check that real-time twitter connectivity is established by inspecting your twitter webhook.

In a browser navigate to 'https://<domain>/probot/twitter/webhook'

If everything is working you will see 'subscribed true' as final status.

## dependencies

Probot back-end has the following essential library dependencies:

 * [Apache Juneau](http://juneau.incubator.apache.org "http://juneau.incubator.apache.org")
   HTTP microservice (based on Jetty) and data marshalling libraries
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

 * [Flink](https://flink.apache.org/ "https://flink.apache.org")
   Stateful Computations over Data Streams
 * [Postgresql](https://www.postgresql.org/ "https://www.postgresql.org/")
   The World's Most Advanced Open Source Relational Database
 * [Postgrest](https://postgrest.org "http://www.postgrest.org")
   PostgREST is a standalone web server that turns your PostgreSQL database directly into a RESTful API.
 * [Zeppelin](https://zeppelin.apache.org/ "https://zeppelin.apache.org")
   Web-based notebook that enables data-driven,
   interactive data analytics and collaborative documents with SQL, Scala and more.


