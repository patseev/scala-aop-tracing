package aop

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import natchez.{EntryPoint, Span}
import natchez.logodin.Log
import io.odin.Logger
import io.odin.consoleLogger
import io.odin.{Level, formatter}

object Main extends IOApp {

  type Init[A] = IO[A]
  type Run[A] = Kleisli[IO, Span[IO], A]

  def run(args: List[String]): IO[ExitCode] =
    buildDependencies.flatMap { case (ep, repo) =>
      program(ep, repo).attempt.as(ExitCode.Success)
    }

  def buildDependencies = {
    implicit val log: Logger[Init] = consoleLogger(formatter = formatter.Formatter.colorful, minLevel = Level.Info)
    for {
      entrypoint <- Log.entryPoint[Init]("app").pure[Init]
      petRepo <- PetRepository.make[Init, Run]
    } yield (entrypoint, petRepo)
  }

  def program(ep: EntryPoint[Init], petRepository: PetRepository[Run]): Init[Unit] =
    ep.root("program").use(span =>
      (for {
        _ <- petRepository.create("Pet1", 10)
        _ <- petRepository.find("Pet1")
        _ <- petRepository.create("Pet3", 15)
        _ <- petRepository.list
        _ <- petRepository.update("Pet1", 20)
        _ <- petRepository.get("Pet2") // boom
      } yield ()).run(span)
    )

}
