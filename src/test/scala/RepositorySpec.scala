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

  "Repository" should "create and retrieve a Customer" in {
    Given("Customer")
    val customer = Customer(name = "Juan Perez")

    When("save to repo and load result")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        customerUpdated <- repository.createCustomer(customer).debug_thread
        saved <- repository.getCustomer(customerUpdated.id.get).debug_thread
      } yield saved.toOption.get
    )

    Then("Value is the expected")
    r.asserting(_.id.isDefined mustBe true)
    r.asserting(_.name mustBe customer.name).debug_thread
    r.asserting(_.dayArtistId.isEmpty mustBe true)
  }

  "Repository" should "update customer artist of the day when no artist" in {
    Given("Client and artists")
    val client = Customer(name = "Juan Perez")

    When("save to repo and update actor of the day")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        customerCreated <- repository.createCustomer(client).debug_thread
        dayArtistId <- repository.updateCustomerDayArtist(customerCreated.id.get).debug_thread
      } yield dayArtistId
    )

    Then("Value is the expected")
    r.asserting(_.isEmpty mustBe true).debug_thread
  }

  "Repository" should "update customer artist of the day when 1 artist 1 update" in {
    Given("Client and artists")
    val client = Customer(name = "Juan Perez")
    val artist1 = Artist(name = "Donna Summer")

    When("save to repo and update actor of the day")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        customerCreated <- repository.createCustomer(client).debug_thread
        artist <- repository.createArtist(artist1).debug_thread
        dayArtistId <- repository.updateCustomerDayArtist(customerCreated.id.get)
      } yield (artist, dayArtistId)
    )

    Then("Value is the expected")
    r.asserting(pair => pair._2.get mustBe pair._1.id.get).debug_thread
  }

  "Repository" should "update customer artist of the day when 1 artist 2 updates" in {
    Given("Client and artists")
    val client = Customer(name = "Juan Perez")
    val artist1 = Artist(name = "Donna Summer")

    When("save to repo and update actor of the day")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        customerCreated <- repository.createCustomer(client).debug_thread
        artist <- repository.createArtist(artist1).debug_thread
        customerId = customerCreated.id.get
        dayArtistId1 <- repository.updateCustomerDayArtist(customerId)
        dayArtistId2 <- repository.updateCustomerDayArtist(customerId)
      } yield (artist, dayArtistId1, dayArtistId2)
    )

    Then("Value is the expected")
    r.asserting(tuple => tuple._2.get mustBe tuple._1.id.get).debug_thread
    r.asserting(tuple => tuple._3.get mustBe tuple._1.id.get).debug_thread
  }

  "Repository" should "update customer artist of the day when 2 artist 2 updates" in {
    Given("Client and artists")
    val client = Customer(name = "Juan Perez")
    val artist1 = Artist(name = "Donna Summer")
    val artist2 = Artist(name = "Fito Paes")

    When("save to repo and update actor of the day")
    val r = transactor.use(t =>
      for {
        _ <- DB.initialize(t).debug_thread
        repository = new Repository(t)
        customerCreated <- repository.createCustomer(client).debug_thread
        artist1 <- repository.createArtist(artist1).debug_thread
        artist2 <- repository.createArtist(artist2).debug_thread
        customerId = customerCreated.id.get
        dayArtistId1 <- repository.updateCustomerDayArtist(customerId)
        dayArtistId2 <- repository.updateCustomerDayArtist(customerId)
      } yield (artist1,artist2, dayArtistId1, dayArtistId2)
    )

    Then("Value is the expected")
    r.asserting(tuple => tuple._3.get mustBe tuple._1.id.get).debug_thread
    r.asserting(tuple => tuple._4.get mustBe tuple._2.id.get).debug_thread
  }


}
