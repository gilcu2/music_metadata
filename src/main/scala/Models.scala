object Models {

  case class Artist(id: Option[Long] = None, name: String)

  case class Track(
    id: Option[Long] = None,
    artistId: Long,
    title: String,
    genreId: Long,
    length: Long
  )

  case class Genre(id: Option[Long] = None, name: String)

  case class ArtistAlias(id: Option[Long] = None, artistId: Long, name: String)

  case class User(id: Option[Long] = None, name: String, nextArtist: Option[Long] = None)

  case object NotFoundError

}
