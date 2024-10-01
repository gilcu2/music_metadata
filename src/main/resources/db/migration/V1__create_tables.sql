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
  title VARCHAR NOT NULL,
  duration INTEGER,
  artist_id INTEGER,
  genre_id INTEGER,
  foreign key (artist_id) references artist(id),
  foreign key (genre_id) references genre(id)
);

CREATE TABLE artist_alias (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  artist_id INTEGER,
  name VARCHAR,
  foreign key (artist_id) references artist(id)
);

CREATE TABLE client (
  id  INTEGER PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR,
  next_artist_id INTEGER
);

