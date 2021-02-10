### Effortless tracing

If you ever had a wish to trace your algebra in a way that every
method creates a new span containing method name, arguments and result, 
here's the easiest way I could come up with.

```
/* All you need to do to create a traced implementation of your algebra */
object Algebra {
    implicit val aspect: Aspect.Function[Algebra, Show] = Derive.aspect

    def make = AlgebraImplementation()
        .weaveFunction
        .mapK(Tracing.traceMethodNamesWithArgs)
}
```

It's based on 
[cats-tagless AOP module](https://github.com/typelevel/cats-tagless/tree/master/core/src/main/scala/cats/tagless/aop)

- Check out example of generated traces in `traces-full.log`
- Check out traced repository implementation in `PetRepository.scala`
- Check out Tracing implementation in `Tracing.scala`

There are multiple concerns:

- What if you don't want to include all arguments? What if they're too big?
- What if arguments contain sensitive data?

You can solve this by defining custom instances of `Show` or some other typeclass for displaying. It adds complexity and kinda defeats the purpose. Maybe there's a better solution.

Additionally: this doesn't replace use of tracer inside of implementation if you need to add some custom info to the span, it's just a smart way of constructing spans "invisibly", so you never have to think about it again
