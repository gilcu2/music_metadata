import Models._
import cats.effect.IO
import doobie.implicits._
import doobie.util.fragment.Fragment
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

  def getArtist(id: Long): IO[Either[RepositoryError, Artist]] = {
    sql"SELECT * FROM artist WHERE id = $id"
      .query[Artist].option.transact(transactor).map {
        case Some(artist) => Right(artist)
        case None => Left(RepositoryError(s"artist $id not found"))
      }
  }

  def updateArtist(artisId:Long,name:String): IO[String] = {
    val sql =
      sql"""
      UPDATE artist
      SET name = $name
      WHERE artist.id = $artisId;
    """

    sql.update
      .withUniqueGeneratedKeys[String]("name")
      .transact(transactor)
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

  def getGenre(id: Long): IO[Either[RepositoryError, Genre]] = {
    sql"SELECT * FROM genre WHERE id = $id"
      .query[Genre].option.transact(transactor).map {
        case Some(genre) => Right(genre)
        case None => Left(RepositoryError(s"genre $id not found"))
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

  def getArtistAlias(id: Long): IO[Either[RepositoryError, ArtistAlias]] = {
    sql"SELECT * FROM artist_alias WHERE id = $id"
      .query[ArtistAlias].option.transact(transactor).map {
        case Some(artistAlias) => Right(artistAlias)
        case None => Left(RepositoryError(s"artist alias $id not found"))
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

  def getTrack(id: Long): IO[Either[RepositoryError, Track]] = {
    sql"SELECT * FROM track WHERE id = $id"
      .query[Track].option.transact(transactor).map {
        case Some(track) => Right(track)
        case None => Left(RepositoryError(s"track $id not found"))
      }
  }

  def getArtistTracks(artistId:Long):Stream[IO,Track]={
    sql"""
          select * from track
          where track.artist_id = $artistId
    """.query[Track].stream.transact(transactor)
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

  def getCustomer(id: Long): IO[Either[RepositoryError, Customer]] = {
    sql"SELECT * FROM customer WHERE id = $id"
      .query[Customer].option.transact(transactor).map {
        case Some(customer) => Right(customer)
        case None => Left(RepositoryError(s"customer $id not found"))
      }
  }

  def updateCustomerDayArtist(customerId: Long): IO[Option[Int]] = {
    val sql =
      sql"""
      UPDATE customer
      SET day_artist_id = (
        WITH row_id AS (
          SELECT artist.id
          FROM  artist, customer
          WHERE customer.id = $customerId
            AND  artist.id > COALESCE(customer.DAY_ARTIST_ID,0)
          ORDER BY artist.id - COALESCE(customer.DAY_ARTIST_ID,0) ASC
          LIMIT 1
        )
        SELECT CASE
	      WHEN (SELECT * FROM row_id LIMIT 1) IS NOT NULL
	        THEN (SELECT * FROM row_id LIMIT 1)
	      ELSE (SELECT artist.id FROM artist LIMIT 1)
        END
      )
      WHERE customer.id = $customerId;
    """

    sql.update
      .withUniqueGeneratedKeys[Option[Int]]("day_artist_id")
      .transact(transactor)
  }

  def countRows(tableName: String): IO[Long] = {
    val sql = sql"select COUNT(*) from " ++ Fragment.const(tableName)
    sql.query[Long].unique.transact(transactor)
  }

  def deleteAllRows(tableName: String): IO[Int] = {
    val sql = sql"delete from " ++ Fragment.const(tableName)
    sql.update.run.transact(transactor)
  }

case class RepositoryError(s:String)