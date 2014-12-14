CREATE TABLE users
(
  id integer NOT NULL,
  email character varying(64) NOT NULL,
  password character varying(128) NOT NULL,
  register_ip character varying,
  last_login_ip character varying,
  register_date date,
  last_login_date date,
  "bluetooth_MAC" character varying,
  CONSTRAINT primarykey_id PRIMARY KEY (id ),
  CONSTRAINT unique_email UNIQUE (email )
);

CREATE TABLE user_profile
(
  user_id integer NOT NULL,
  alias character varying(30),
  birthdate timestamp without time zone,
  gender character varying,
  nationality character varying,
  lookingfor character varying,
  about character varying,
  height integer,
  weight integer,
  interests character varying,
  avatar character varying,
  CONSTRAINT user_id_primarykey PRIMARY KEY (user_id ),
  CONSTRAINT user_id_fk FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE user_links
(
  userid_1 integer NOT NULL,
  userid_2 integer NOT NULL,
  link_time timestamp without time zone,
  lastmsg_time timestamp without time zone,
  lastmsg_content character varying(500),
  CONSTRAINT user_link_primarykey PRIMARY KEY (userid_1 , userid_2 )
);

CREATE TABLE user_msg_queue
(
  id_from integer NOT NULL,
  id_to integer NOT NULL,
  send_time timestamp without time zone NOT NULL,
  content character varying(500),
  msgid serial NOT NULL,
  CONSTRAINT msgid_primary_key PRIMARY KEY (msgid )
);

CREATE TABLE user_requests
(
  "requesterID" integer NOT NULL,
  "targetID" integer,
  found_time timestamp without time zone,
  seen integer DEFAULT 0,
  CONSTRAINT "pk_requesterID" PRIMARY KEY ("requesterID" )
);


