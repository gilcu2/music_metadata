import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.{GivenWhenThen, funspec}
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import cats.syntax.all._
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.circe.parser._
import cats.effect._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._

class ServerSpec extends funspec.AsyncFunSpec with AsyncIOSpec with GivenWhenThen with Matchers {

  trait AnswerMaker[F[_]] {
    def sayHello(name: String): F[String]
  }

  def httpRoutes[F[_]](maker: AnswerMaker[F])(implicit
    F: Async[F]
  ): HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "hello" / name =>
    maker.sayHello(name).map(r => Response(status = Status.Ok).withEntity(r.asJson))
  }

  describe("service") {

    it("return the hello msg") {
      Given("name and AnswerMaker")
      val name = "Juan"
      val maker = new AnswerMaker[IO] {
        def sayHello(name: String): IO[String] = IO.pure(s"Hi $name")
      }

      And("expected answer")
      val expected=s"Hi $name"

      When("get the response")
      val response = httpRoutes[IO](maker).orNotFound.run(
        Request(method = Method.GET, uri = Uri.unsafeFromString(s"/hello/$name"))
      )

      Then("it is expected")
      response.asserting(_.status mustBe Status.Ok)
      response.asserting(r=>toObjectUnsafe[String](r.body)  mustBe expected)
    }

  }

  def toObjectUnsafe[A:Decoder](stream:fs2.Stream[IO,Byte]):A= {
    val s=(stream.compile.toVector.unsafeRunSync().map(_.toChar)).mkString
    parse(s).toOption.get.as[A].toOption.get
  }

}
