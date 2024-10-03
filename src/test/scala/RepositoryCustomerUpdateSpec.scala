import cats.effect._
import debug._
import doobie.hikari.HikariTransactor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import cats.effect.testing.scalatest.AsyncIOSpec
import Models._

class RepositoryCustomerUpdateSpec
  extends funspec.AsyncFunSpec
  with AsyncIOSpec
  with GivenWhenThen
  with Matchers {

  val transactor: Resource[IO, HikariTransactor[IO]] = DB.transactor()
  val artistTable="artist"
  val customerTable="customer"

  describe("Repository Customer update") {

    it("update customer artist of the day when no artist") {
      Given("Client and artists")
      val client = Customer(name = "Juan Perez")

      When("save to repo and update actor of the day")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t).debug_thread
          repository = new Repository(t)
          customerCreated <- repository.createCustomer(client).debug_thread
          dayArtistId     <- repository.updateCustomerDayArtist(customerCreated.id.get).debug_thread
        } yield dayArtistId
      )

      Then("Value is the expected")
      r.asserting(_.isEmpty mustBe true).debug_thread
    }

    it("update customer artist of the day when 1 artist 1 update") {
      Given("Client and artists")
      val client  = Customer(name = "Juan Perez")
      val artist1 = Artist(name = "Donna Summer")

      When("save to repo and update actor of the day")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t).debug_thread
          repository = new Repository(t)
          customerCreated <- repository.createCustomer(client).debug_thread
          artist          <- repository.createArtist(artist1).debug_thread
          dayArtistId     <- repository.updateCustomerDayArtist(customerCreated.id.get)
        } yield (artist, dayArtistId)
      )

      Then("Value is the expected")
      r.asserting(pair => pair._2.get mustBe pair._1.id.get).debug_thread
    }

    it("update customer artist of the day when 1 artist 2 updates") {
      Given("Client and artists")
      val client  = Customer(name = "Juan Perez")
      val artist1 = Artist(name = "Donna Summer")

      When("save to repo and update actor of the day")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t).debug_thread
          repository = new Repository(t)
          _ <- repository.deleteAllRows(customerTable)
          _ <- repository.deleteAllRows(artistTable)
          customerCreated <- repository.createCustomer(client).debug_thread
          artist          <- repository.createArtist(artist1).debug_thread
          customerId = customerCreated.id.get
          dayArtistId1 <- repository.updateCustomerDayArtist(customerId)
          dayArtistId2 <- repository.updateCustomerDayArtist(customerId)
        } yield (artist, dayArtistId1, dayArtistId2)
      )

      Then("Value is the expected")
      r.asserting(tuple => tuple._2.get mustBe tuple._1.id.get).debug_thread
      r.asserting(tuple => tuple._3.get mustBe tuple._1.id.get).debug_thread
    }

    it("update customer artist of the day when 2 artist 2 updates") {
      Given("Client and artists")
      val client  = Customer(name = "Juan Perez")
      val artist1 = Artist(name = "Donna Summer")
      val artist2 = Artist(name = "Fito Paes")


      When("save to repo and update actor of the day")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t).debug_thread
          repository = new Repository(t)
          _ <- repository.deleteAllRows(customerTable)
          _ <- repository.deleteAllRows(artistTable)
          customerCreated <- repository.createCustomer(client).debug_thread
          artist1         <- repository.createArtist(artist1).debug_thread
          artist2         <- repository.createArtist(artist2).debug_thread
          customerId = customerCreated.id.get
          dayArtistId1 <- repository.updateCustomerDayArtist(customerId)
          dayArtistId2 <- repository.updateCustomerDayArtist(customerId)
        } yield (artist1, artist2, dayArtistId1, dayArtistId2)
      )

      Then("Value is the expected")
      r.asserting(tuple => tuple._3.get mustBe tuple._1.id.get).debug_thread
      r.asserting(tuple => tuple._4.get mustBe tuple._2.id.get).debug_thread
    }

    it("update customer artist of the day when 2 artist 3 updates") {
      Given("Client and artists")
      val client  = Customer(name = "Juan Carlos")
      val artist1 = Artist(name = "Donna Summer")
      val artist2 = Artist(name = "Fito Paes")


      When("save to repo and update actor of the day")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t).debug_thread
          repository = new Repository(t)
          _ <- repository.deleteAllRows(customerTable)
          _ <- repository.deleteAllRows(artistTable)
          customerCreated <- repository.createCustomer(client).debug_thread
          artist1         <- repository.createArtist(artist1).debug_thread
          artist2         <- repository.createArtist(artist2).debug_thread
          customerId = customerCreated.id.get
          dayArtistId1 <- repository.updateCustomerDayArtist(customerId)
          dayArtistId2 <- repository.updateCustomerDayArtist(customerId)
          dayArtistId3 <- repository.updateCustomerDayArtist(customerId)
        } yield (artist1, artist2, dayArtistId1, dayArtistId2,dayArtistId3)
      )

      Then("Value is the expected")
      r.asserting(tuple => tuple._3.get mustBe tuple._1.id.get).debug_thread
      r.asserting(tuple => tuple._4.get mustBe tuple._2.id.get).debug_thread
      r.asserting(tuple => tuple._5.get mustBe tuple._1.id.get).debug_thread
    }

  }

}
