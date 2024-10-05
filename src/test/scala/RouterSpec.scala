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
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[String](r.body) mustBe expected)
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
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[Long](r.body) mustBe expected)
    }

    it("produce custom message in other route") {
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
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.NotFound)
      r.asserting(r => toObjectUnsafe[String](r.body) mustBe expected)
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
          id = toObjectUnsafe[Artist](createdResponse.body).id.get
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist/$id"))
          ).debug()
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[Artist](r.body).name mustBe name)
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
          id = toObjectUnsafe[Artist](createdResponse.body).id.get
          _ <-routes.run(
            Request(method = Method.PUT, uri = Uri.unsafeFromString(s"/artist/$id")).withEntity(newName.asJson)
          ).debug()
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist/$id"))
          ).debug()
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[Artist](r.body).name mustBe newName)
    }

    it("create and retrieve an artist alias") {
      Given("artist")
      val name   = "Juan Gabriel"
      val artist = Artist(name = name)
      val aliasName="The crazy"

      When("create and get artist")
      val r = transactor.use(t =>
        for {
          _ <- DB.initialize(t)
          repository = new Repository(t)
          routes     = new Routes(repository).routes.orNotFound
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          ).debug()
          artistId = toObjectUnsafe[Artist](createdArtist.body).id.get
          artistAlias=ArtistAlias(name = aliasName,artistId = artistId)
          createdAlias <- routes.run(
            Request(method = Method.POST, uri = uri"/artist_alias").withEntity(artistAlias.asJson)
          ).debug()
          aliasId = toObjectUnsafe[ArtistAlias](createdAlias.body).id.get
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/artist_alias/$aliasId"))
          ).debug()
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[ArtistAlias](r.body).name mustBe aliasName)
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
          genreId = toObjectUnsafe[Genre](createdResponse.body).id.get
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/genre/$genreId"))
          ).debug()
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[Genre](r.body).name mustBe genreName)
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
          genreId = toObjectUnsafe[Genre](createdGenre.body).id.get
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          ).debug()
          artistId = toObjectUnsafe[Artist](createdArtist.body).id.get
          track = Track(title = title, duration = duration, artistId = artistId, genreId = genreId)
          createdTrack <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track.asJson)
          ).debug()
          trackId = toObjectUnsafe[Track](createdTrack.body).id.get
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/track/$trackId"))
          ).debug()
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[Track](r.body).title mustBe title)
    }

    it("retrieve artist tracks") {
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
          genreId = toObjectUnsafe[Genre](createdGenre.body).id.get
          createdArtist <- routes.run(
            Request(method = Method.POST, uri = uri"/artist").withEntity(artist.asJson)
          ).debug()
          artistId = toObjectUnsafe[Artist](createdArtist.body).id.get
          track = Track(title = title, duration = duration, artistId = artistId, genreId = genreId)
          createdTrack <- routes.run(
            Request(method = Method.POST, uri = uri"/track").withEntity(track.asJson)
          ).debug()
          trackId = toObjectUnsafe[Track](createdTrack.body).id.get
          response <- routes.run(
            Request(method = Method.GET, uri = Uri.unsafeFromString(s"/track/$trackId"))
          ).debug()
        } yield response
      )

      Then("it is expected")
      r.asserting(_.status mustBe Status.Ok)
      r.asserting(r => toObjectUnsafe[Track](r.body).title mustBe title)
    }

  }

  def toObjectUnsafe[A: Decoder](stream: fs2.Stream[IO, Byte]): A = {
    val s = stream.compile.toVector.unsafeRunSync().map(_.toChar).mkString
    println(s"toObject: $s")
    parse(s).toOption.get.as[A].toOption.get
  }

}
