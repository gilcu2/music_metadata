import Models.{Artist, ArtistAlias, Genre, Track}
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.util.transactor
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import io.circe.parser._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.{GivenWhenThen, funspec}

class RouterSpec extends funspec.AsyncFunSpec with AsyncIOSpec with GivenWhenThen with Matchers {

  val transactor: Resource[IO, HikariTransactor[IO]] = DB.transactor()

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

    it("produce custom message if other route") {
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
          ).debug()
          id <- createdResponse.as[String].map(decode[Artist](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist/$id"))
          ).debug()
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
          ).debug()
          id <- createdResponse.as[String].map(decode[Artist](_).id.get)
          _ <-routes.run(
            Request(method = Method.PUT, uri = Uri.unsafeFromString(s"/artist/$id")).withEntity(newName.asJson)
          ).debug()
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist/$id"))
          ).debug()
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
          ).debug()
          artistId <- createdArtistResponse.as[String].map(decode[Artist](_).id.get)
          artistAlias=ArtistAlias(name = aliasName,artistId = artistId)
          createdAlias <- routes.run(
            Request(method = Method.POST, uri = uri"/artist_alias").withEntity(artistAlias.asJson)
          ).debug()
          aliasId <- createdAlias.as[String].map(decode[ArtistAlias](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist_alias/$aliasId"))
          ).debug()
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
          ).debug()
          genreId <- createdResponse.as[String].map(decode[Genre](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/genre/$genreId"))
          ).debug()
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
          ).debug()
          genreId <- createdGenre.as[String].map(decode[Genre](_).id.get)
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          ).debug()
          artistId <- createdArtist.as[String].map(decode[Artist](_).id.get)
          track = Track(title = title, duration = duration, artistId = artistId, genreId = genreId)
          createdTrack <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track.asJson)
          ).debug()
          trackId <- createdTrack.as[String].map(decode[Track](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/track/$trackId"))
          ).debug()
          track <- response.as[String].map(decode[Track])
        } yield (response, track)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.title mustBe title)
    }

    it("create and retrieve an track") {
      Given("genre")
      val artistName = "Silvio"
      val artist = Artist(name = artistName)
      val genreName = "Balada"
      val genre = Genre(name = genreName)
      val title = "Unicornio azul"
      val duration = 180

      When("create and retireve customer")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes = new Routes(repository).routes.orNotFound
          createdGenre <- routes.run(
            Request(method = Method.POST, uri = uri"/genre").withEntity(genre.asJson)
          ).debug()
          genreId <- createdGenre.as[String].map(decode[Genre](_).id.get)
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          ).debug()
          artistId <- createdArtist.as[String].map(decode[Artist](_).id.get)
          track = Track(title = title, duration = duration, artistId = artistId, genreId = genreId)
          createdTrack <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track.asJson)
          ).debug()
          trackId <- createdTrack.as[String].map(decode[Track](_).id.get)
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/track/$trackId"))
          ).debug()
          track <- response.as[String].map(decode[Track])
        } yield (response, track)
      )

      Then("it is expected")
      r.asserting(_._1.status mustBe Status.Ok)
      r.asserting(_._2.title mustBe title)
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
          ).debug()
          genreId <- createdGenre.as[String].map(decode[Genre](_).id.get)
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          ).debug()
          artistId <- createdArtist.as[String].map(decode[Artist](_).id.get)
          track1 = Track(title = title1, duration = duration, artistId = artistId, genreId = genreId)
          _ <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track1.asJson)
          ).debug()
          track2 = Track(title = title2, duration = duration, artistId = artistId, genreId = genreId)
          _ <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track2.asJson)
          ).debug()
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/tracks/$artistId"))
          ).debug()
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
