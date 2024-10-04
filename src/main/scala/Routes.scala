import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import Models._
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.headers.`Content-Type`

class Routes(repository: Repository) {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "hello" / name =>
      IO.pure(s"Hi $name").flatMap(r => Ok(r.asJson))

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

  }

  private def mapResult[A:Encoder](result: Either[NotFoundError.type, A]): IO[Response[IO]] = {
    result match {
      case Left(NotFoundError) => NotFound()
      case Right(o) => Ok(o.asJson)
    }
  }
}