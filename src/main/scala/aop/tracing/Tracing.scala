package aop.tracing

import cats.tagless.aop.Aspect
import cats.{Monad, Show, ~>}
import natchez.{Trace, TraceValue}
import cats.implicits._

object Tracing {
  /* Creates spans containing method names */
  def traceMethodNames[F[_]: Trace: Monad, Dom[_], Cod[_]]: ~>[Aspect.Weave[F, Dom, Cod, *], F] = new ~>[Aspect.Weave[F, Dom, Cod, *], F] {
    def apply[A](fa: Aspect.Weave[F, Dom, Cod, A]): F[A] =
      Trace[F].span(s"${fa.algebraName}.${fa.codomain.name}") {
        fa.codomain.target
      }
  }

  /* Creates spans containing method names, arguments and result */
  def traceMethodNamesWithArgs[F[_]: Trace: Monad]: ~>[Aspect.Weave.Function[F, Show, *], F] = new ~>[Aspect.Weave.Function[F, Show, *], F] {
    def apply[A](fa: Aspect.Weave.Function[F, Show, A]): F[A] =
      Trace[F].span(s"${fa.algebraName}.${fa.codomain.name}") {
        val args =
          fa.domain.flatMap(_.map(arg => s"args.${arg.name}" -> TraceValue.StringValue(arg.instance.show(arg.target.value))))

        Trace[F].put(args: _*) *>
          fa.codomain.target.flatTap(res =>
            Trace[F].put("result" -> TraceValue.StringValue(fa.codomain.instance.show(res)))
          )
      }
  }

}
