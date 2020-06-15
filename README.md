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

    mvn clean package
    
Run the following to build the front-end from source.

    yarn install

Run the following to prepare docker containers for deployment.

    docker-compose build

## running

Run the following to deploy the stack to local docker environment.

    docker-compose up -d

## dependencies

Probot back-end has the following essential library dependencies:
 * [Apache Juneau](http://juneau.incubator.apache.org "http://juneau.incubator.apache.org")
   HTTP microservice (based on Jetty) and data marshalling libraries
 * [Apache Spark](http://spark.apache.org "http://spark.apache.org")
   Data engineering / data science framework supporting batch and Stream processing
 * [Apache Streams](http://streams.apache.org "http://streams.apache.org")
   Twitter SDK, pojos, and connectivity

Probot front-end has the following essential dependencies:

 * [React Admin](https://marmelab.com/react-admin/ "https://marmelab.com/react-admin/")
   A Web Framework for B2B applications
 * [Material UI](https://material-ui.com/ "https://material-ui.com/")
   React components for faster and easier web development.

Probot relies on several supporting run-time dependencies:

 * [Postgresql](https://www.postgresql.org/ "https://www.postgresql.org/")
   The World's Most Advanced Open Source Relational Database
 * [Postgrest](https://postgrest.org "http://www.postgrest.org")
   PostgREST is a standalone web server that turns your PostgreSQL database directly into a RESTful API.


