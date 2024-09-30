import cats.effect.{IO, IOApp}

object Hello extends IOApp.Simple {
  val run = IO.println("Hello, World!")
}