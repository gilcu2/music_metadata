import cats.effect._
import debug._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import cats.effect.testing.scalatest.AsyncIOSpec
import Models._

class RepositorySpec extends flatspec.AsyncFlatSpec with AsyncIOSpec
  with  GivenWhenThen with Matchers {

  val transactor: Resource[IO, HikariTransactor[IO]] = DB.transactor()

  "Repository" should "create and retrieve an Artist" in {
    Given("Artist")
    val artist = Artist(name="Donna Summer")

    When("save to repo and load result")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        returned <- repository.createArtist(artist).debug_thread
        saved <- repository.getArtist(returned.id.get).debug_thread
      } yield saved.toOption.get
    )

    Then("Value is the expected")
    r.asserting(_.name shouldBe artist.name).debug_thread

  }

}
