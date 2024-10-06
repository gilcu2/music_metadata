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
    }

    it("update Artist") {
      Given("Artist")
      val artist = Artist(name = "Donna Summer")

      And("new name")
      val newName = s"${artist.name} The Best"

      When("save to repo and load result")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          returned <- repository.createArtist(artist)
          updatedName <- repository.updateArtist(returned.id.get, newName)
        } yield updatedName
      )

      Then("Value is the expected")
      r.asserting(_ mustBe newName)
    }

    it("create and retrieve a Genre") {
      Given("Genre")
      val genre = Genre(name = "Salsa")

      When("save to repo and load result")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          returned <- repository.createGenre(genre)
          saved <- repository.getGenre(returned.id.get)
        } yield saved.toOption.get
      )

      Then("Value is the expected")
      r.asserting(_.name mustBe genre.name)
    }

    it("create and retrieve an ArtistAlias") {
      Given("Artist and alias name")
      val artist = Artist(name = "Donna Summer")
      val aliasName = "The best"

      When("save to repo and load result")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          artistUpdated <- repository.createArtist(artist)
          alias = ArtistAlias(artistId = artistUpdated.id.get, name = aliasName)
          aliasUpdated <- repository.createArtistAlias(alias)
          saved <- repository.getArtistAlias(aliasUpdated.id.get)
        } yield saved.toOption.get
      )

      Then("Value is the expected")
      r.asserting(_.name mustBe aliasName)
    }

    it("create and retrieve an Track") {
      Given("Track data and  foreign keys")
      val artist = Artist(name = "Donna Summer")
      val genre = Genre(name = "Rock")
      val title = "Let it be"
      val length = 180

      When("save to repo and load result")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          artistUpdated <- repository.createArtist(artist)
          genreUpdated <- repository.createGenre(genre)
          track = Track(title = title, duration = length, artistId = artistUpdated.id.get, genreId = genreUpdated.id.get)
          trackUpdated <- repository.createTrack(track)
          saved <- repository.getTrack(trackUpdated.id.get)
        } yield saved.toOption.get
      )

      Then("Value is the expected")
      r.asserting(_.title mustBe title)
    }

    it("retrieve artist tracks") {
      Given("Track data and  foreign keys")
      val artist = Artist(name = "Donna Summer")
      val genre = Genre(name = "Rock")
      val title1 = "Let it be"
      var title2 = "Yesterday"
      val length = 180

      When("save to repo and load result")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          artistUpdated <- repository.createArtist(artist)
          genreUpdated <- repository.createGenre(genre)
          track1 = Track(title = title1, duration = length, artistId = artistUpdated.id.get, genreId = genreUpdated.id.get)
          _ <- repository.createTrack(track1)
          track2 = Track(title = title2, duration = length, artistId = artistUpdated.id.get, genreId = genreUpdated.id.get)
          _ <- repository.createTrack(track2)
          result <- repository.getArtistTracks(artistUpdated.id.get).compile.toList
        } yield result
      )

      Then("Value is the expected")
      r.asserting(_.map(_.title).toSet mustBe Set(title1,title2))
    }


    it("create and retrieve a Customer") {
      Given("Customer")
      val customer = Customer(name = "Juan Perez")

      When("save to repo and load result")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          customerUpdated <- repository.createCustomer(customer)
          saved <- repository.getCustomer(customerUpdated.id.get)
        } yield saved.toOption.get
      )

      Then("Value is the expected")
      r.asserting(_.id.isDefined mustBe true)
      r.asserting(_.name mustBe customer.name)
      r.asserting(_.dayArtistId.isEmpty mustBe true)
    }


    it("delete and count rows of table") {
      Given("table name and data")
      val customerTable = "customer"
      val artistTable = "artist"
      val aliasTable = "artist_alias"
      val trackTable = "track"
      val artist1 = Artist(name = "Donna Summer")
      val artist2 = Artist(name = "Fito Paes")

      When("insert rows and count")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          _ <- repository.deleteAllRows(customerTable)
          _ <- repository.deleteAllRows(aliasTable)
          _ <- repository.deleteAllRows(trackTable)
          _ <- repository.deleteAllRows(artistTable)
          _ <- repository.createArtist(artist1)
          _ <- repository.createArtist(artist2)
          count1 <- repository.countRows(artistTable)
          count2 <- repository.deleteAllRows(artistTable)
          count3 <- repository.countRows(artistTable)
        } yield (count1, count2, count3)
      )

      Then("result is expected")
      r.asserting(_._1 mustBe 2)
      r.asserting(_._2 mustBe 2)
      r.asserting(_._3 mustBe 0)
    }
  }

}
