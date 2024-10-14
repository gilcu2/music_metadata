
import cats.effect._
import Util._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import cats.effect.testing.scalatest.AsyncIOSpec
import Models._

class RepositorySimpleSpec extends funspec.AsyncFunSpec with AsyncIOSpec
  with  GivenWhenThen with Matchers {

  val transactor: Resource[IO, HikariTransactor[IO]] = DB.transactor()

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
      it("fail returning Artist for absent id") {
        Given("absent id")
        val artistId = -1

        When("load result")
        val r = transactor.use(t =>
          for {
            _ <- DB.initialize(t)
            repository = new Repository(t)
            saved <- repository.getArtist(artistId)
          } yield saved
        )

        Then("Value is the expected")
        r.asserting(_.isLeft mustBe true)
      }

  }
}

