# Twitter data backfill

## first determine the twitter id of your 'seed' account

http://gettwitterid.com

## use seedid notebook to generate /data/seed_userid.txt

http://localhost:8890/#/notebook/2FEN25W6E

## use CollectFollowers notebook to retrieve all followers to followers.jsonl folder

http://localhost:8890/#/notebook/2FJEJYZVS

## use CollectFriends notebook to retrieve all friends to friends.jsonl folder

http://localhost:8890/#/notebook/2FFK4E6KD

## use CollectUserTimeline notebook to retrieve tweets to user_timeline.jsonl folder

http://localhost:8890/#/notebook/2FEU6669Q

## use schema notebook to configure database schema

http://localhost:8890/#/notebook/2FFD4X1F1

## use LoadFriends notebook to load friends.jsonl to friends_json database table

http://localhost:8890/#/notebook/2FHHYTRKA

## use LoadFollowers notebook to load followers.jsonl to followers_json database table

http://localhost:8890/#/notebook/2FHHYTRKA

## use views notebook to create database views

http://localhost:8890/#/notebook/2FFD4X1F1
