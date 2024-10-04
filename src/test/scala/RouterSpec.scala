import Models.Artist
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.util.transactor
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import io.circe.parser._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.{funspec, GivenWhenThen}

class RouterSpec extends funspec.AsyncFunSpec with AsyncIOSpec with GivenWhenThen with Matchers {

  val transactor: Resource[IO, HikariTransactor[IO]] = DB.transactor()

  describe("service") {

    it("return the hello msg") {
      Given("name")
      val name = "Juan"

      And("expected answer")
      val expected = s"Hi $name"

      When("get the response")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository)
          response <- routes.routes.orNotFound.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/hello/$name"))
          )
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[String](r.body) mustBe expected)
    }

    it("create and retrieve an artist") {
      Given("artist")
      val name   = "Juan Gabriel"
      val artist = Artist(name = name)

      When("create and get artist")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository).routes.orNotFound
          createdResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          )
          id = toObjectUnsafe[Artist](createdResponse.body).id
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist/$id"))
          )
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[Artist](r.body).name mustBe name)
    }

  }

  def toObjectUnsafe[A: Decoder](stream: fs2.Stream[IO, Byte]): A = {
    val s = stream.compile.toVector.unsafeRunSync().map(_.toChar).mkString
    parse(s).toOption.get.as[A].toOption.get
  }

}
