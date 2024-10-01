import Models.{Artist, ArtistAlias, Client, Genre, NotFoundError, Track}
import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream

class Repository(transactor: Transactor[IO]) {

  def createArtist(artist: Artist): IO[Artist] = {
    val name = artist.name
    val sql_query =
      sql"""
    INSERT INTO artist (
          name
    )
    VALUES (
      $name
    );
    """
    val sql_query_updated = sql_query
      .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => artist.copy(id = Some(id))
      }
  }

  def getArtist(id: Long): IO[Either[NotFoundError.type, Artist]] = {
    sql"SELECT * FROM artist WHERE id = $id"
      .query[Artist].option.transact(transactor).map {
        case Some(artist) => Right(artist)
        case None => Left(NotFoundError)
      }
  }

  def createGenre(genre: Genre): IO[Genre] = {
    val name = genre.name
    val sql_query =
      sql"""
    INSERT INTO genre (
          name
    )
    VALUES (
      $name
    );
    """
    val sql_query_updated = sql_query
      .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => genre.copy(id = Some(id))
      }
  }

  def getGenre(id: Long): IO[Either[NotFoundError.type, Genre]] = {
    sql"SELECT * FROM genre WHERE id = $id"
      .query[Genre].option.transact(transactor).map {
        case Some(genre) => Right(genre)
        case None => Left(NotFoundError)
      }
  }

  def createArtistAlias(alias: ArtistAlias): IO[ArtistAlias] = {
    val aliasName = alias.name
    val artistId = alias.artistId
    val sql_query =
      sql"""
    INSERT INTO artist_alias (
          artist_id,
          name
    )
    VALUES (
      $artistId,
      $aliasName
    );
    """
    val sql_query_updated = sql_query
      .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => alias.copy(id = Some(id))
      }
  }

  def getArtistAlias(id: Long): IO[Either[NotFoundError.type, ArtistAlias]] = {
    sql"SELECT * FROM artist_alias WHERE id = $id"
      .query[ArtistAlias].option.transact(transactor).map {
        case Some(artistAlias) => Right(artistAlias)
        case None => Left(NotFoundError)
      }
  }

  def createTrack(track: Track): IO[Track] = {
     val sql_query =
      sql"""
    INSERT INTO track (
        title,
        duration,
        artist_id,
        genre_id
    )
    VALUES (
      ${track.title},
      ${track.duration},
      ${track.artistId},
      ${track.genreId}
    );
    """
    val sql_query_updated = sql_query
      .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => track.copy(id = Some(id))
      }
  }

  def getTrack(id: Long): IO[Either[NotFoundError.type, Track]] = {
    sql"SELECT * FROM track WHERE id = $id"
      .query[Track].option.transact(transactor).map {
        case Some(track) => Right(track)
        case None => Left(NotFoundError)
      }
  }

  def createClient(client: Client): IO[Client] = {
    val name = client.name
    val next_artist=client.nextArtist
    val sql_query =
      sql"""
    INSERT INTO client (
          name,
          next_artist
    )
    VALUES (
      $name,
      $next_artist
    );
    """
    val sql_query_updated = sql_query
      .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => client.copy(id = Some(id))
      }
  }

  def getClient(id: Long): IO[Either[NotFoundError.type, Client]] = {
    sql"SELECT * FROM client WHERE id = $id"
      .query[Client].option.transact(transactor).map {
        case Some(client) => Right(client)
        case None => Left(NotFoundError)
      }
  }

  def getNextArtist(id: Long): IO[Either[NotFoundError.type, Artist]] = {
    sql"""SELECT * FROM artist WHERE id > $id order by id limit 1"""

      .query[Artist].option.transact(transactor).map {
        case Some(artist) => Right(artist)
        case None => Left(NotFoundError)
      }
  }

  def updateClientNextArtist(userId: Long): IO[Either[NotFoundError.type, Artist]] = {
    sql"""
         UPDATE client
         SET next_artist_id = (
            SELECT artist.id
            FROM artist
            WHERE artist.id > client.next_artist_id
            ORDER BY artist.id
         )
         """
      .query[Artist].option.transact(transactor).map {
        case Some(artist) => Right(artist)
        case None => Left(NotFoundError)
      }
  }

  //  def getAllStats(airport_name_begin: String = ""): Stream[IO, AirportReviewCount] = {
//    sql"""
//         SELECT airport_name, count(*) as review_count
//         FROM review
//         WHERE airport_name LIKE ${airport_name_begin + "%"}
//         GROUP BY airport_name
//      """
//      .query[AirportReviewCount].stream.transact(transactor)
//  }
//
//  def getAirportStats(airport_name: String)
//  : IO[Either[AirportNotFoundError.type, AirportStats]] = {
//    sql"""
//         SELECT
//            airport_name,
//            count(*) as review_count,
//            AVG(overall_rating) as average_overall_rating,
//            SUM(recommended)
//         FROM review
//         WHERE airport_name = $airport_name
//      """
//      .query[AirportStats].option.transact(transactor).map {
//        case Some(stat) => Right(stat)
//        case None => Left(AirportNotFoundError)
//      }
//  }
//
//  def getAirportReviews(airport_name: String, maybe_minimum_overall_rating: Option[Double] = None): Stream[IO, AirportReview] = {
//    val minimum_overall_rating = maybe_minimum_overall_rating.getOrElse(0.0)
//    sql"""
//         SELECT
//            airport_name,
//            overall_rating,
//            date,
//            content,
//            author,
//            author_country
//         FROM review
//         WHERE airport_name = $airport_name AND overall_rating >= $minimum_overall_rating
//      """
//      .query[AirportReview].stream.transact(transactor)
//  }

}
