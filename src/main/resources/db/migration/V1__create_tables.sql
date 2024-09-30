CREATE TABLE artist (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR NOT NULL
);

CREATE TABLE genre (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR NOT NULL
);

CREATE TABLE track (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  artist_id INTEGER,
  title VARCHAR NOT NULL,
  genre_id INTEGER,
  length INTEGER,
  foreign key (artist_id) references artist(id),
  foreign key (genre_id) references genre(id)
);

CREATE TABLE artist_alias (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  artist_id INTEGER,
  alias VARCHAR,
  foreign key (artist_id) references artist(id)
);

CREATE TABLE user (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR,
  next_artist INTEGER
);

