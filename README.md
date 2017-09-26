# probot

Profile Bot (aka **probot**) is a software package you can run provide insights into your twitter activity stream and social graph with simple reports.
 
**probot** can bind to your incoming direct messages using the twitter account activity API, watch for messages that match preset patterns, and reply with short messages and links to report endpoints.

## basics

**probot** is implemented with [Scala](https://scala.io) 
and packaged with [Apache maven](http://maven.apache.org).

Probot has the following essential dependencies:
 * [Akka](http://akka.io "http://akka.io")
   Asychronous messaging between probot components
 * [Apache Juneau](http://juneau.incubator.apache.org "http://juneau.incubator.apache.org") 
   HTTP microservice (based on Jetty) and data marshalling libraries 
 * [Apache Streams](http://streams.apache.org "http://streams.apache.org") 
   Twitter SDK, pojos, and connectivity
   
## building

At the moment **probot** builds and runs on snapshots (*this will change shortly*).

Check out probot and run the following to build the code.  This should pull snapshot artifacts from repository.apache.org

    mvn clean package
    
You may want to check out, build, and install the latest snapshot of Juneau and Streams (following those projects' instructions) if you want to add features that require modifications to those libraries.

## running

From the probot source directory, start probot with:

    java -Dconfig.file=probot.conf -cp dist/probot-jar-with-dependencies.jar org.apache.juneau.microservice.RestMicroservice application.cfg

probot.conf should be a hocon file similar to src/test/resources/example.conf

Twitter is fairly strict about the SSL properties of your endpoint.

[Ngrok](https://ngrok.com "https://ngrok.com") is an easy way to ensure your probot is compliant.

## features

/TopDomains

    rank domains by the number of link profile shared
    
/TopHashtags

    rank hashtags by the number of posts by profile that used them
    
/TopMentions

    rank users by the number of times profile mentioned them
    
/TopPosts

    rank posts by profile according to metrics:
        * most favorites
        * most retweets
        
/TopFollowing

    rank friends or followers of profile according to metrics:
        * most favorites
        * most followers
        * most friends
        * most lists
        * most posts
