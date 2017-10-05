# probot

Profile Bot (aka **probot**) is a software package you can run provide insights into your twitter activity stream and social graph with simple reports.
 
**probot** can bind to your incoming direct messages using the twitter account activity API, watch for messages that match preset patterns, and reply with short messages and links to report endpoints.

## basics

**probot** is implemented with [Scala](https://scala.io) 
and packaged with [Apache Maven](http://maven.apache.org).

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

## challenges/opportunities

**probot** is currently hard-wired to use the timeline and network of the credentialed account.

- *Challenge*: Casual users probably won't run a bot server, but will be curious about their own accounts.
- *Opportunity*: Teach **probot** to generate reports about accounts other than itself.

**probot** creates in-memory datasets at the moment a dataset is accessed.

- *Challenge*: Using java objects on heap is expensive, limits the absolute size of the data.
- *Opportunity*: Persist raw data to a folder than can be mounted on start-up.
- *Opportunity*: Add [Apache Spark](http://spark.apache.org) to the stack and build reports with computations over DataFrames.

**probot** can only access 3200 posts per user, due to twitter API limitations.

- *Challenge*: Prolific twitter users has much longer timelines than this.
- *Opportunity*: Add support for using downloaded twitter archives, in conjunction with API connection.

**probot** has access to only one twitter API key.

- *Challenge*: API throughput and rate-limits constrain what can be done on-demand under this model
- *Opportunity*: Allow operaters with access to multiple keys to provide more than one.
- *Opportunity*: Report over available data, while expanding available data as a background process.
- *Opportunity*: Encourage users interacting with the bot to oauth their accounts to the probot app.

**probot**'s code base is single module and several classes are approaching unwieldy.

- *Challenge*: Make it easier to add capabilities to **probot** without needing to build from source.
- *Opportunity*: Define key traits in the core module and discover all implementing classes on the classpath via reflection.

**probot**'s plain-language interface is only available via Direct Message.

- *Challenge*: Not everyone who might want to interact with your probot will be on twitter or be logged in on every device.
- *Opportunity*: Add a feature to the web interface that provides a 'command bar' (with auto-complete?) 

**probot**'s reporting on Top Domains is sub-optimal.

- *Challenge*: Url shorteners are ubiquitous in tweet content and twitter's API expanded_url does not expand most of them.
- *Opportunity*: Use juneau to traverse redirect chains of the links in each post to find the original domain.

**probot** only integrates with Twitter.

- *Challenge*: Most internet users maintain profiles on multiple networks.
- *Opportunity*: Switch the canonical profile format from Twitter API to Activity Streams.
- *Opportunity*: Use additional providers from [Apache Streams](http://streams.apache.org), adding messaging support.

**probot**'s pattern matching on messages is fairly rudimentary.

- *Challenge*: It's not obvious how one should interact with **probot** to take advantage of its capabilities.
- *Opportunity*: Create a structured menu / help system similar to 'man' to instruct new engagers.
- *Opportunity*: Write better documentation.

- *Challenge*: Iterate toward a robust plain-language interface.
- *Opportunity*: Use sentence / grammer parsing by [Apache OpenNLP](http://opennlp.apache.org) to recognize event case.
- *Opportunity*: Add a step whereby **probot** responds to the user repeating what it thinks it was asked.
- *Opportunity*: Seek confirmation and/or suggest i.e. 'did you mean ...'.



