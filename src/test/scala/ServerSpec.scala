import Configs.Config
import Models._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.unsafe.IORuntime.global
import cats.effect.{ExitCode, IO}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.literal._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, funspec}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

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

  private val clientResource  = BlazeClientBuilder[IO].resource

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

    it("create and retrieve an Artist1") {
      Given("Artist")
      val artist = Artist(name = "Juan Gabriel")
      val requestPost =
        Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/artist"))
          .withEntity(artist.asJson)

      When("Post and get the Artist")
      val r = clientResource.use ( client =>
        for {
        response <- client.use(_.expect[Json](requestPost))
      } yield response
    )

      Then("result is the expected")
      assert(r.status mustBe Status.Ok)
    }


  }

  private def resultHandler(result: Either[Throwable, ExitCode]): Unit =
    result.left.foreach(t => logger.error("Executing the http server failed", t))

}
