
CREATE TABLE "probot"."profile" (
                                   "_id" VARCHAR(36) NOT NULL UNIQUE,
                                   "as_id" VARCHAR(255) NOT NULL,
                                   "as_name" VARCHAR(255),
                                   "as_summary" VARCHAR(255),
                                   "as_url" VARCHAR(255),
                                   "as_location_name" VARCHAR(255),
                                   "apst_handle" VARCHAR(255),
                                   "apst_followers" INTEGER,
                                   "apst_friends" INTEGER,
                                   "apst_likes" INTEGER,
                                   "apst_posts" INTEGER,
                                   "apst_lists" INTEGER,
                                   "probot_blocking" BOOLEAN,
                                   "probot_following" BOOLEAN,
                                   "probot_followrequestsent" BOOLEAN,
                                   PRIMARY KEY (_id)
);
