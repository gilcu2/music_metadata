import Models.{Artist, NotFoundError}
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
    IO.println("Before update")
    val sql_query_updated = sql_query
      .update
    IO.println("After update")
    sql_query_updated
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .map { id => artist.copy(id = Some(id))
      }
  }

  def getArtist(id: Long): IO[Either[NotFoundError.type, Artist]] = {
    sql"SELECT * FROM artist WHERE id = $id"
      .query[Artist].option.transact(transactor).map {
        case Some(review) => Right(review)
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
