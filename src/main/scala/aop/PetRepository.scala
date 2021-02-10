package aop

import cats.Show
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.tagless.Derive
import cats.tagless.aop.Aspect
import cats.tagless.implicits._
import natchez.Trace
import aop.tracing.Tracing

trait PetRepository[F[_]] {
  def find(name: String): F[Option[Pet]]
  def get(name: String): F[Pet]
  def list: F[List[Pet]]
  def create(name: String, age: Int): F[Unit]
  def update(name: String, age: Int): F[Unit]
}

object PetRepository {
  implicit private val aspect: Aspect.Function[PetRepository, Show] = Derive.aspect

  def make[I[_]: Sync, F[_]: Sync: Trace]: I[PetRepository[F]] =
    InMemoryPetRepository
      .make[I, F]
      .map(_.weaveFunction.mapK(Tracing.traceMethodNamesWithArgs)) /* Tracing.traceMethodNames */
}


/** Dummy ref-based implementation */
private object InMemoryPetRepository {
  def make[I[_]: Sync, F[_]: Sync]: I[PetRepository[F]] = {
    Ref.in[I, F, List[Pet]](List.empty).map { petsRef =>
      new PetRepository[F] {
        def find(name: String): F[Option[Pet]] =
          petsRef.get.map(_.find(_.name == name))

        def get(name: String): F[Pet] =
          find(name).flatMap {
            case Some(pet) => pet.pure[F]
            case None => Sync[F].raiseError(new RuntimeException(s"Pet $name is not found"))
          }

        def list: F[List[Pet]] =
          petsRef.get

        def create(name: String, age: Int): F[Unit] =
          petsRef.update(_.appended(Pet(name, age)))

        def update(name: String, age: Int): F[Unit] =
          petsRef.update(_.filterNot(_.name == name).appended(Pet(name, age)))
      }
    }
  }
}
