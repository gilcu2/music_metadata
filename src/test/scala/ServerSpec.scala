import Configs.Config
import Models._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{ExitCode, IO}
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.literal._
import io.circe.parser._
import io.circe.syntax._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, funspec}
import org.slf4j.LoggerFactory


class ServerSpec
  extends funspec.AsyncFunSpec
  with AsyncIOSpec
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll
  with Eventually {

  private val configFile = "test.conf"
  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))

  private lazy val config  = Config.load(configFile).use(config => IO.pure(config)).unsafeRunSync()
  private val logger       = LoggerFactory.getLogger(classOf[ServerSpec])
  private lazy val rootUrl = s"http://${config.server.host}:${config.server.port}/api"

  private val clientResource =BlazeClientBuilder[IO].resource

  override def beforeAll(): Unit = {

    Server.create(configFile).unsafeRunAsync(resultHandler)
    eventually {
      clientResource
        .use(_.statusFromUri(Uri.unsafeFromString(s"$rootUrl/hello/test")))
        .unsafeRunSync() shouldBe Status.Ok
    }
    ()
  }

  describe("Repository server") {

    it("retrieve hello") {
      Given("name")
      val name = "juan"

      When("retrieve hello")
      val r = clientResource.use(client =>
        for {
          result <- client.expect[String](s"$rootUrl/hello/$name")
            .map(decode[String])
        } yield result
      )

      Then("result is the expected")
      r.asserting(_ mustBe s"Hi $name")
    }

    it("create and retrieve an Artist") {
      Given("Artist")
      val artist = Artist(name = "Juan Gabriel")
      val requestPost =
        Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/artist"))
          .withEntity(artist.asJson)

      When("Post and get the Artist")
      val r = clientResource.use ( client =>
        for {
          createdArtist <- client.expect[String](requestPost)
            .map(decode[Artist])
          id = createdArtist.id.get
          retrieved <- client.expect[String](s"$rootUrl/artist/$id")
            .map(decode[Artist])
        } yield retrieved
      )

      Then("result is the expected")
      r.asserting(_.name mustBe artist.name)
    }



  }

  private def resultHandler(result: Either[Throwable, ExitCode]): Unit =
    result.left.foreach(t => logger.error("Executing the http server failed", t))

  private def decode[A: Decoder](s: String): A = {
    parse(s).toOption.get.as[A].toOption.get
  }

}
