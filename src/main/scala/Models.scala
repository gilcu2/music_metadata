object Models {

  case class Artist(id: Option[Long] = None, name: String)

  case class Track(
                    id: Option[Long] = None,
                    title: String,
                    duration: Long,
                    artistId: Long,
                    genreId: Long,
  )

  case class Genre(id: Option[Long] = None, name: String)

  case class ArtistAlias(id: Option[Long] = None, artistId: Long, name: String)

  case class Client(id: Option[Long] = None, name: String, nextArtist: Long = 0)

  case object NotFoundError

}
