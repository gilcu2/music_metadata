CREATE TABLE IF NOT EXISTS artist (
  id  BIGSERIAL PRIMARY KEY,
  name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
  id  BIGSERIAL PRIMARY KEY,
  name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS track (
  id  BIGSERIAL PRIMARY KEY,
  title VARCHAR NOT NULL,
  duration INTEGER,
  artist_id INTEGER,
  genre_id INTEGER,
  foreign key (artist_id) references artist(id),
  foreign key (genre_id) references genre(id)
);

CREATE TABLE IF NOT EXISTS artist_alias (
  id  BIGSERIAL PRIMARY KEY,
  artist_id INTEGER,
  name VARCHAR NOT NULL,
  foreign key (artist_id) references artist(id)
);

CREATE TABLE IF NOT EXISTS customer (
  id  BIGSERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  day_artist_id INTEGER,
  foreign key (day_artist_id) references artist(id)
);

