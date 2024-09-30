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
    Given("Artist and alias name")
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

  "Repository" should "create and retrieve an Track" in {
    Given("Track data and  foreign keys")
    val artist = Artist(name = "Donna Summer")
    val genre = Genre(name = "Rock")
    val title = "Let it be"
    val length = 180

    When("save to repo and load result")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        artistUpdated <- repository.createArtist(artist).debug_thread
        genreUpdated <- repository.createGenre(genre).debug_thread
        track = Track(title = title, duration = length, artistId = artistUpdated.id.get, genreId = genreUpdated.id.get)
        trackUpdated <- repository.createTrack(track).debug_thread
        saved <- repository.getTrack(trackUpdated.id.get).debug_thread
      } yield saved.toOption.get
    )

    Then("Value is the expected")
    r.asserting(_.title mustBe title).debug_thread
  }

  "Repository" should "create and retrieve a Client" in {
    Given("Client")
    val client = Client(name = "Juan Perez")

    When("save to repo and load result")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        clientUpdated <- repository.createClient(client).debug_thread
        saved <- repository.getClient(clientUpdated.id.get).debug_thread
      } yield saved.toOption.get
    )

    Then("Value is the expected")
    r.asserting(_.name mustBe client.name).debug_thread
  }

}
