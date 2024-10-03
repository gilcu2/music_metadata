import Models.{Artist, ArtistAlias, Customer, Genre, NotFoundError, Track}
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

  def createCustomer(customer: Customer): IO[Customer] = {
    val name = customer.name
    val dayArtistId: Option[Long] = customer.dayArtistId
    val sqlQuery =
      sql"""
    INSERT INTO customer (
          name,
          day_artist_id
    )
    VALUES (
      $name,
      $dayArtistId
    );
    """
    val sql_query_updated = sqlQuery
      .update
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => customer.copy(id = Some(id))
      }
  }

  def getCustomer(id: Long): IO[Either[NotFoundError.type, Customer]] = {
    sql"SELECT * FROM customer WHERE id = $id"
      .query[Customer].option.transact(transactor).map {
        case Some(customer) => Right(customer)
        case None => Left(NotFoundError)
      }
  }

  def updateCustomerDayArtist(customerId: Long): IO[Option[Int]] = {
    sql"""
      UPDATE customer
      SET day_artist_id = (
        SELECT CASE
            WHEN (SELECT COUNT(*) FROM ARTIST) = 0 THEN null
            WHEN (SELECT COUNT(*) FROM ARTIST) = 1 THEN (SELECT artist.id FROM artist)
            ELSE
            (SELECT artist.id
             FROM  artist, customer
             WHERE customer.id = $customerId
                  AND  artist.id > COALESCE(customer.DAY_ARTIST_ID,0)
             ORDER BY artist.id - COALESCE(customer.DAY_ARTIST_ID,0) ASC
             LIMIT 1
            )
       END
      )
       WHERE customer.id = $customerId;
    """
    .update
    .withUniqueGeneratedKeys[Option[Int]]("day_artist_id")
    .transact(transactor)
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
