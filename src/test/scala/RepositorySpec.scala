import cats.effect._
import debug._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
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
    r.asserting(_.name mustBe artist.name).debug_thread
  }

  "Repository" should "create and retrieve a Genre" in {
    Given("Genre")
    val genre = Genre(name = "Salsa")

    When("save to repo and load result")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        returned <- repository.createGenre(genre).debug_thread
        saved <- repository.getGenre(returned.id.get).debug_thread
      } yield saved.toOption.get
    )

    Then("Value is the expected")
    r.asserting(_.name mustBe genre.name).debug_thread
  }

  "Repository" should "create and retrieve an ArtistAlias" in {
    Given("Artist and alias")
    val artist = Artist(name = "Donna Summer")
    val aliasName = "The best"

    When("save to repo and load result")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        artistUpdated <- repository.createArtist(artist).debug_thread
        alias = ArtistAlias(artistId = artistUpdated.id.get, name = aliasName)
        aliasUpdated <- repository.createArtistAlias(alias).debug_thread
        saved <- repository.getArtistAlias(aliasUpdated.id.get).debug_thread
      } yield saved.toOption.get
    )

    Then("Value is the expected")
    r.asserting(_.name mustBe aliasName).debug_thread
  }

}
