import Configs.Config
import cats.effect._
import doobie.hikari.HikariTransactor
import com.zaxxer.hikari.HikariConfig
import org.flywaydb.core.Flyway

object DB {
  def transactor(config: Config): Resource[IO, HikariTransactor[IO]] =
    for {
      hikariConfig <- Resource.pure {
        val hikariConfig = new HikariConfig()
        hikariConfig.setDriverClassName(config.database.driver)
        hikariConfig.setJdbcUrl(config.database.url)
        hikariConfig.setUsername(config.database.user)
        hikariConfig.setPassword(config.database.password)
        hikariConfig
      }
      xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
    } yield xa

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
      }
    }
  }

}
