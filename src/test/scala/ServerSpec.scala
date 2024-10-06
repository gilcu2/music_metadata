import Configs.Config
import Models._
import cats.effect.testing.scalatest.AsyncIOSpec
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
  private lazy val client  = BlazeClientBuilder[IO].resource
  private lazy val rootUrl = s"http://${config.server.host}:${config.server.port}/api"

  override def beforeAll(): Unit = {
    Server.create(configFile).unsafeRunAsync(resultHandler)
    eventually {
      client
        .use(_.statusFromUri(Uri.unsafeFromString(s"$rootUrl/hello/test")))
        .unsafeRunSync() shouldBe Status.Ok
    }
    ()
  }

  describe("Repository server") {

    it("create and retrieve an Artist") {
      Given("Artist")
      val artist=Artist(name = "Juan Gabriel")

      When("Post and get the Artist")
      val requestPost =
        Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/artist"))
          .withEntity(artist.asJson)
      val jsonPost   = client.use(_.expect[Json](requestPost)).unsafeRunSync()
      val resultPost = jsonPost.as[Artist].toOption.get
      val id = resultPost.id.get
      val requestGet =
        Request[IO](method = Method.GET, uri = Uri.unsafeFromString(s"$rootUrl/artist/$id"))
      val jsonGet   = client.use(_.expect[Json](requestGet)).unsafeRunSync()
      val resultGet = jsonGet.as[Artist].toOption.get

      Then("result is the expected")
      assert(resultGet.name == artist.name)
    }

    it("create and retrieve an Artist1") {
      Given("Artist")
      val artist=Artist(name = "Juan Gabriel")

      When("Post and get the Artist")
      val r = for {
        request= Request[IO] (method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/artist"))
      .withEntity(artist.asJson)
        val jsonPost = client.use(_.expect[Json] (requestPost)).unsafeRunSync()
        val resultPost = jsonPost.as[Artist].toOption.get
        val id = resultPost.id.get
        val requestGet =
        Request[IO] (method = Method.GET, uri = Uri.unsafeFromString(s"$rootUrl/artist/$id"))
        val jsonGet = client.use(_.expect[Json] (requestGet)).unsafeRunSync()
        val resultGet = jsonGet.as[Artist].toOption.get
      } yield

      Then("result is the expected")
      assert(resultGet.name == artist.name)
    }

  }

  private def resultHandler(result: Either[Throwable, ExitCode]): Unit =
    result.left.foreach(t => logger.error("Executing the http server failed", t))

}
