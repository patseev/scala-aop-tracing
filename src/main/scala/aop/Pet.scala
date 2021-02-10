package aop

import cats.Show

case class Pet(name: String, age: Int)

object Pet {
  implicit val show: Show[Pet] = Show.show(pet => s"Pet { name = ${pet.name}, age = ${pet.age} }")
}
