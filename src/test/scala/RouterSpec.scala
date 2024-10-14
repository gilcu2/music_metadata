import Configs.Config
import Models._
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.hikari.HikariTransactor
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.{GivenWhenThen, funspec}

class RouterSpec extends funspec.AsyncFunSpec with AsyncIOSpec with GivenWhenThen with Matchers {

  val configFile = "test.conf"
  val transactor: Resource[IO, HikariTransactor[IO]] = for {
    config <- Config.load(configFile)
    transactor <- DB.transactor(config)
  } yield transactor

  describe("service") {

    it("return the hello msg") {
      Given("name")
      val name = "Juan"

      And("expected answer")
      val expected = s"Hi $name"

      When("get the response")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository)
          response <- routes.routes.orNotFound.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/hello/$name"))
          )
          msg <- response.as[String].map(decode[String])
        } yield (response, msg)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2 mustBe expected)
    }

    it("duplicate number") {
      Given("number")
      val number = 3

      And("expected answer")
      val expected = 2*number

      When("get the response")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository)
          response <- routes.routes.orNotFound.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/duplicate/$number"))
          )
          duplication <- response.as[String].map(decode[Long])
        } yield (response, duplication)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2 mustBe expected)
    }

    it("produce custom message when unrecognized route route") {
      Given("number")
      val number = 3

      And("expected answer")
      val expected = s"Route not found: /duplicate1/$number"

      When("get the response")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository)
          response <- routes.routes.orNotFound.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/duplicate1/$number"))
          )
          responseBody <- response.as[String].map(decode[String])
        } yield (response, responseBody)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.NotFound)
      r.asserting(_._2 mustBe expected)
    }

    it("create and retrieve an artist") {
      Given("artist")
      val name   = "Juan Gabriel"
      val artist = Artist(name = name)

      When("create and get artist")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository).routes.orNotFound
          createdResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          )
          id <- createdResponse.as[String].map(decode[Artist](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist/$id"))
          )
          artist <- response.as[String].map(decode[Artist])
        } yield (response, artist)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.name mustBe name)
    }

    it("update an artist") {
      Given("artist")
      val name   = "Juan Gabriel"
      val artist = Artist(name = name)
      val newName = "Juan Gabriel the crazy"

      When("create and get artist")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository).routes.orNotFound
          createdResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          )
          id <- createdResponse.as[String].map(decode[Artist](_).id.get)
          _ <-routes.run(
            Request(method = Method.PUT, uri = Uri.unsafeFromString(s"/artist/$id")).withEntity(newName.asJson)
          )
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist/$id"))
          )
          artist <- response.as[String].map(decode[Artist])
        } yield (response, artist)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.name mustBe newName)
    }

    it("create and retrieve an artist alias") {
      Given("artist")
      val name   = "Juan Gabriel"
      val artist = Artist(name = name)
      val aliasName="The crazy"

      When("create and get artist alias")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository).routes.orNotFound
          createdArtistResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          )
          artistId <- createdArtistResponse.as[String].map(decode[Artist](_).id.get)
          artistAlias=ArtistAlias(name = aliasName,artistId = artistId)
          createdAlias <- routes.run(
            Request(method = Method.POST, uri = uri"/artist_alias").withEntity(artistAlias.asJson)
          )
          aliasId <- createdAlias.as[String].map(decode[ArtistAlias](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist_alias/$aliasId"))
          )
          alias <- response.as[String].map(decode[ArtistAlias])
        } yield (response, alias)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.name mustBe aliasName)
    }

    it("create and retrieve an genre") {
      Given("genre")
      val genreName = "Balada"
      val genre = Genre(name = genreName)

      When("create and get track")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes = new Routes(repository).routes.orNotFound
          createdResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/genre").withEntity(genre.asJson)
          )
          genreId <- createdResponse.as[String].map(decode[Genre](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/genre/$genreId"))
          )
          genre <- response.as[String].map(decode[Genre])
        } yield (response, genre)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.name mustBe genreName)
    }

    it("create and retrieve an track") {
      Given("genre")
      val artistName = "Silvio"
      val artist = Artist(name = artistName)
      val genreName = "Balada"
      val genre = Genre(name = genreName)
      val title = "Unicornio azul"
      val duration = 180

      When("create and get track")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes = new Routes(repository).routes.orNotFound
          createdGenre <- routes.run(
            Request(method = Method.POST, uri = uri"/genre").withEntity(genre.asJson)
          )
          genreId <- createdGenre.as[String].map(decode[Genre](_).id.get)
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          )
          artistId <- createdArtist.as[String].map(decode[Artist](_).id.get)
          track = Track(title = title, duration = duration, artistId = artistId, genreId = genreId)
          createdTrack <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track.asJson)
          )
          trackId <- createdTrack.as[String].map(decode[Track](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/track/$trackId"))
          )
          track <- response.as[String].map(decode[Track])
        } yield (response, track)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.title mustBe title)
    }

    it("create and retrieve an customer") {
      Given("customer")
      val customerName = "Juan"
      val customer = Customer(name = customerName)

      When("create and retrieve customer")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes = new Routes(repository).routes.orNotFound
          createdCustomer <- routes.run(
            Request(method = Method.POST, uri = uri"/customer").withEntity(customer.asJson)
          )
          customerId <- createdCustomer.as[String].map(decode[Artist](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/customer/$customerId"))
          )
          customer <- response.as[String].map(decode[Customer])
        } yield (response, customer)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.name mustBe customerName)
    }

    it("update customer artist of the day") {
      Given("Customer and artists")
      val customer = Customer(name = "Juan Carlos")
      val artist1 = Artist(name = "Donna Summer")
      val artist2 = Artist(name = "Fito Paes")
      val customerTable = "customer"
      val artistTable = "artist"
      val aliasTable = "artist_alias"
      val trackTable = "track"

      When("create and update artist of the day")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          _ <- repository.deleteAllRows(customerTable)
          _ <- repository.deleteAllRows(aliasTable)
          _ <- repository.deleteAllRows(trackTable)
          _ <- repository.deleteAllRows(artistTable)
          routes = new Routes(repository).routes.orNotFound
          createdResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist1.asJson)
          )
          idArtist1 <- createdResponse.as[String].map(decode[Artist](_).id.get)
          createdResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist2.asJson)
          )
          idArtist2 <- createdResponse.as[String].map(decode[Artist](_).id.get)
          createdResponse <- routes.run(
            Request(method = Method.POST, uri = uri"/customer").withEntity(customer.asJson)
          )
          createdCustomer <- createdResponse.as[String].map(s => decode[Customer](s))
          (idCustomer, dayArtist0) = (createdCustomer.id.get, createdCustomer.dayArtistId)
          update1Response <- routes.run(
            Request(method = Method.PUT, uri = Uri.unsafeFromString(s"/customer/$idCustomer"))
          )
          dayArtist1 <- update1Response.as[String].map(s => decode[Option[Long]](s))
          update2Response <- routes.run(
            Request(method = Method.PUT, uri = Uri.unsafeFromString(s"/customer/$idCustomer"))
          )
          dayArtist2 <- update2Response.as[String].map(s => decode[Option[Long]](s))
          update3Response <- routes.run(
            Request(method = Method.PUT, uri = Uri.unsafeFromString(s"/customer/$idCustomer"))
          )
          dayArtist3 <- update3Response.as[String].map(s => decode[Option[Long]](s))
        } yield (idArtist1, idArtist2, dayArtist0, dayArtist1, dayArtist2, dayArtist3)
      )

      Then("it is expected")
      r.asserting(_._3 mustBe None)
      r.asserting(tuple => tuple._4 mustBe Some(tuple._1))
      r.asserting(tuple => tuple._5 mustBe Some(tuple._2))
      r.asserting(tuple => tuple._6 mustBe Some(tuple._1))
    }

    it("retrieve artist tracks") {
      Given("genre")
      val artistName = "Silvio"
      val artist = Artist(name = artistName)
      val genreName = "Balada"
      val genre = Genre(name = genreName)
      val title1 = "Unicornio azul"
      val title2 = "La masa"
      val duration = 180

      When("create tracks")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes = new Routes(repository).routes.orNotFound
          createdGenre <- routes.run(
            Request(method = Method.POST, uri = uri"/genre").withEntity(genre.asJson)
          )
          genreId <- createdGenre.as[String].map(decode[Genre](_).id.get)
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          )
          artistId <- createdArtist.as[String].map(decode[Artist](_).id.get)
          track1 = Track(title = title1, duration = duration, artistId = artistId, genreId = genreId)
          _ <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track1.asJson)
          )
          track2 = Track(title = title2, duration = duration, artistId = artistId, genreId = genreId)
          _ <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track2.asJson)
          )
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/tracks/$artistId"))
          )
          tracks <- response.as[String].map(decode[List[Track]])
        } yield (response, tracks)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.map(_.title).toSet mustBe Set(title1, title2))
    }

  }

  def decode[A: Decoder](s: String): A = {
    parse(s).toOption.get.as[A].toOption.get
  }

}
