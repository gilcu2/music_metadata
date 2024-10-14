
import Configs.Config
import cats.effect._
import Util._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import cats.effect.testing.scalatest.AsyncIOSpec
import Models._

class RepositoryItSpec extends funspec.AsyncFunSpec with AsyncIOSpec
  with GivenWhenThen with Matchers {

  val configFile = "it_test.conf"
  val transactor: Resource[IO, HikariTransactor[IO]] = for {
    config <- Config.load(configFile)
    transactor <- DB.transactor(config)
  } yield transactor

  describe("Repository simple functionalities") {

    it("create and retrieve an Artist") {
      Given("Artist")
      val artist = Artist(name = "Donna Summer")

      When("save to repo and load result")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          returned <- repository.createArtist(artist)
          saved <- repository.getArtist(returned.id.get)
        } yield saved.toOption.get
      )

      Then("Value is the expected")
      r.asserting(_.name mustBe artist.name)
    }
  }
}

