import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import Models._
import fs2.Stream
import fs2.text.utf8
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.headers.`Content-Type`


class Routes(repository: Repository) {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "hello" / name =>
      IO.pure(s"Hi $name").flatMap(r => Ok(r.asJson)).debug()

    case GET -> Root / "duplicate" / LongVar(number) =>
      IO.pure(2*number).flatMap(r => Ok(r.asJson)).debug()

    case req@POST -> Root / "artist" =>
      for {
        artist <- req.decodeJson[Artist]
        createdArtist <- repository.createArtist(artist)
        response <- Created(createdArtist.asJson)
      } yield response

    case GET -> Root / "artist" / LongVar(id) =>
      for {
        getResult <- repository.getArtist(id)
        response <- mapResult(getResult)
      } yield response

    case other =>  IO(Response(
      Status.NotFound,
      body = Stream(s"Route not found: ${other.uri}".asJson.noSpaces).through(utf8.encode),
      headers = Headers(`Content-Type`(MediaType.application.json) :: Nil)
    ))

  }

  private def mapResult[A:Encoder](result: Either[RepositoryError, A]): IO[Response[IO]] = {
    result match {
      case Left(error) => IO(Response(
        Status.NotFound,
        body = Stream(error.asJson.noSpaces).through(utf8.encode),
        headers = Headers(`Content-Type`(MediaType.application.json) :: Nil)
      ))
      case Right(o) => Ok(o.asJson)
    }
  }
}