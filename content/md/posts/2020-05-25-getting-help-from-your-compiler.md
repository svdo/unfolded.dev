{:title "Implementing Form Validation Without Conditional Logic"
 :subtitle "Using clever data types to reduce the possibilities for mistakes"
 :layout :post
 :tags  ["functional", "typescript", "either", "validation", "react"]
 :toc true}

*I'm learning about functional programming. I'm having loads of fun doing that,
and I also think my code is improving because of it. In this post I want to
share something that I learned about using Either to do form validation in
TypeScript.*

<p style="text-align: right">
  <a href="https://github.com/svdo/either-validation-demo" target="sourcecode">
  <i class="fab fa-github" style="font-size: 200%">&nbsp;</i>Source code for this post</a></p>

**[Update June 4, 2020]** When continuing to work on this code, I improved one
important aspect. At the end of this post I added the section [Making the
validation functions reusable](#making-the-validation-functions-reusable) to
explain. Also the code in the [github repo][demo-repo] was updated to reflect
this.

## Less Bugs

Today I want to show you a neat technique for form validation. Almost every web
app will eventually have a form somewhere, and the user input of that form
usually needs to be validated. You can do that by writing lots of conditions all
over the place, but in this post I will show you that you can do it _without any
single condition_ except for the validation logic itself. But the rest of the
app, including the UI, doesn't have conditional logic. That's great, because it
significantly reduces the number of mistakes (bugs) you can make. To understand
this post, you don't need any specific knowledge or experience.

## Clever Data Types

Let's explore a concrete example. Suppose you're writing a GUI that contains a
form, and you need to validate the elements of that form. Making it even more
specific: a registration web page where a user can sign up for a new account by
providing information like e-mail, phone number, and a password (twice, to
prevent typing mistakes).

This kind of code can easily become a big mess of conditional logic: **if** the
e-mail field is not empty **and** it is not a valid e-mail address, **then**
show an error. And **if** the phone number field is not empty **and** it is not
a valid phone number, **then** show another error. And **if** the password is
not empty **and** does not meet the minimum requirements, **then** show yet
another error. And **if** any of the above **if**s were true, **then** disable
the submit button. And so on. Yes, you can write that code, but it's going to be
ever so easy to forget cases and combinations of cases.

So why not use a cleverly designed data type? I'm not making this up myself,
smarter people have already done so, and they call the type `Either`. Typically
it represents *either* an error *or* a valid value. The error case is (by
convention) called the *left* of the `Either`, the valid value is the *right*.
So a variable of the `Either` type could either be `left('that e-mail address is
invalid')` or it could be `right('me@example.com')`.

This data type comes with an API. This API is designed in such a way that it
becomes hard to "do the wrong thing". For example, the function that allows you
to get the valid value out of the `Either` if it's a `right`, *requires* you to
also specify what should happen when it's a `left`. You can't forget, because it
won't compile[^exceptjs].

So how do you use that? Back to our registration page, let's define the
following validation function (in TypeScript):

```typescript
const emailValid = (email: string): Either<string, string> =>    // (1)
  emailAddress.parseOneAddress(email) !== null                   // (2)
    ? right(email)                                               // (3)
    : left('invalid email address')                              // (4)
```

On line `(1)`, you see the declaration of the validation function. It's argument
is that string that we want to validate, and the return type is an `Either` of
which both the left hand side and the right hand side are a `string`. That's
because the error (left) is a string, and the valid value, namely the email
address, is also a string. On line `(2)` we call a library function to parse the
provided string and check whether the return value is `null`. When it's not, the
email address was valid and we return the right on line `(3)`, or when it is
`null` then we return the left on line `(4)`. Nice and simple.

Let's define a few more.

```typescript
const phoneValid = (phone: string): Either<string, string> =>
  /[0-9]{10}/.test(phone) ? right(phone) : left('invalid phone number')

const equalPasswords = (
  p1: string,
  p2: string
): Either<string, string> => (p1 === p2 ? right(p1) : left('passwords differ'))

const minLength = (s: string): Either<string, string> =>
  s.length >= 8 ? right(s) : left('password must be at least 8 characters')

const oneCapital = (s: string): Either<string, string> =>
  /[A-Z]/g.test(s) ? right(s) : left('password must have at least one capital')

const oneNumber = (s: string): Either<string, string> =>
  /[0-9]/g.test(s) ? right(s) : left('password must have at least one number')
```

Zooming in on the password, we note that there's a bunch of different ways in
which a password can be wrong, and we want to capture all of them. So we want to
call all of the related password-checking functions, each of which will give an
`Either` as a result, and then *combine* those `Either`s. Combining the `right`
is easy. After all, if it's a `right` then the right contains the password, so
the combined either should still have the password as a value. How do we combine
the left? By converting the left from a string to an array of strings, and then
concatenating those arrays. This conversion is called *lifting*: we lift the
single error string into an array containing that single error string. And now
that the lefts are lifted into arrays, we can combine them by concatenating
those arrays. If you're interested in the source code of `lift`, you can find it
in the appendix below, but for now it's good enough if you conceptually
understand what it does: it converts `left('passwords differ')` into
`left(['passwords differ'])`, for example.

## The Scary Bit 😱

The library that I've been using for this style of (functional) programming in
TypeScript, is [fp-ts][fp-ts]. It has some concepts that may seem scary if
you've never seen them before. But fear not, it's actually not that difficult to
understand and use.

First up: [semigroup][semigroup]. A semigroup is just something that supports
*combining* things. Or `concat`ing if you like. You can see why we need this,
right? We need to combine the error strings in the `left`s of all the validation
results.

Then there's the function `getValidation()`, which returns a special kind of
`Either` that knows that the `left`s have to be combined somehow. How, you ask?
That's defined by the *semigroup* that you give it.

<div style="text-align: center; font-size: 3em;">🤯</div>

Ok ok, relax, you'll get there. The following piece of code just means: give me
a special `Validation`-kind of an `Either` that knows how to combine `lefts` by
concatenating the string arrays that they contain:

```typescript
import { getValidation } from 'fp-ts/lib/Either'
import { getSemigroup } from 'fp-ts/lib/NonEmptyArray'

const applicativeValidation = () => getValidation(getSemigroup<string>())
```

If you want more details / explanation, I recommend reading their
[article][either-vs-validation] about the topic.

The last scary piece of the puzzle consists of `sequenceS` and `sequenceT`. They
are basically the same, so let's start with `sequenceT` (`T` is for tuple, aka
array). It needs the `applicativeValidation` we just defined, and then a list of
our validation functions. It spits out an array of the return values of the
validation functions. Well, sort of. It puts it in an `Either` first. That is,
unless one or more of those `Either`s was a left, in that case `sequenceT` spits
the left which is the array of errors that the applicative validation has
created.

"Example!" you shout in desperation? Sure thing:

```typescript
sequenceT(applicativeValidation())(         // (1)
      lift(minLength)(p1),                  // (2)
      lift(oneCapital)(p1),                 // (3)
      lift(oneNumber)(p1),                  // (4)
      lift2(equalPasswords)(p1, p2)         // (5)
    )
```

This uses the simple validation functions that we defined above, as well as
`lift` which we already mentioned, and `lift2` which is the same thing but then
to lift a validation function that takes two parameters instead of just
one.[^lift2] It returns an `Either`. When everything is ok, it returns:

```typescript
right([p1,p1,p1,p1])
```

One `p1` because the return value of line `(2)` is
`right(p1)`, one because line `(3)` returns `right(p1)`, one for line `(4)` and
one for line `(5)` (because `equalPasswords` also returns `p1` when all was ok).
However, if one or more of those return values was a `left`, the whole thing is
a `left`. For example when the passwords don't have a number or an upper case
character, the return value is:

```typescript
left(['password must have at least one capital',
      'password must have at least one number'])
```

In order to use this as a building block for the entire form validation, we want
a `passwordValid` function that returns only `right(p1)`, not the array of four
`p1`s, so we `map` the `Either` and get the following password validation
function.

```typescript
import { pipe } from 'fp-ts/lib/pipeable'
import { constant } from 'fp-ts/lib/function'

function passwordValid (
  p1: string,
  p2: string
): Either<NonEmptyArray<string>, string> {
  return pipe(
    sequenceT(applicativeValidation())(
      lift(minLength)(p1),
      lift(oneCapital)(p1),
      lift(oneNumber)(p1),
      lift2(equalPasswords)(p1, p2)
    ),
    map(constant(p1))
  )
}
```

This `map` conceptually is the same as the one you already know for arrays: it
takes whatever value is captured (the things in the array, or the right-hand
side of the either), and applies a function to it. The `pipe` function pipes the
value of an expression into a pipeline of functions. See
[example][pipe-example]. The function `constant` is a function that always
returns the given value. This is needed because `map` requires a *function* to
be applied to the element (the `right` in this case). Instead of `constant(p1)`
we could also write `() => p1`, but this way the intention is more explicit.

Now it's a small step to define the entire form validation function. In this
case we use `sequenceS` instead of `sequenceT`. Where `sequenceT` makes an array
of elements in the `right`, `sequenceS` makes an object:

```typescript
export function validateRegistrationData (
  email: string,
  phone: string,
  p1: string,
  p2: string,
  consent: boolean
): Either<NonEmptyArray<string>, RegistrationData> {
  return sequenceS(applicativeValidation())({
    email: lift(emailValid)(email),
    phone: lift(phoneValid)(phone),
    password: passwordValid(p1, p2)
  })
}
```

Look at that! We have a function that takes all the individual form input
values, and it spits out an object that happens to be my internal user-profile
representation, or a list of errors!

<div style="text-align: center; font-size: 3em;">😎</div>

## Using This In The UI

For the UI part I'll be using React. Because of it's declarative nature it
matches very well with the above function style of doing validation. The
component that we are creating is called `RegistrationForm`. It uses a bunch of
state hooks for the individual form input values:

```tsx
export const RegistrationForm = () => {
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password1, setPassword1] = useState('')
  const [password2, setPassword2] = useState('')

  [...]

  return <Form>
        <Form.Field>
          <label>Email:</label>
          <Input name="email" value={email}
            onChange={(_, { value }) => setEmail(value)}
          />
        </Form.Field>
        <Form.Field>
          <label>Phone:</label>
          <Input name="mobile" value={phone}
            onChange={(_, { value }) => setPhone(value)}
          />
        </Form.Field>
        <Form.Input label='Password' type="password" value={password1}
          onChange={(_, { value }) => setPassword1(value)}
        />
        <Form.Input
          label='Password again'
          type="password"
          value={password2}
          onChange={(_, { value }) => setPassword2(value)}
        />
        <Button primary content='Register' />
  </Form>
}
```

Easy enough. Now for the interesting part. After declaring the variables for the
various state hooks, we add the call to our validation function. You don't need
to put the type there, TypeScript will infer it for you, but I did so anyway to
remind you of what our validation function is returning.

```typescript
  const validationResult: Either<
    NonEmptyArray<string>,
    RegistrationData
  > = validateRegistrationData(email, phone, password1, password2)
```

In Semantic UI, the `Form` element has to know whether there's an error in the
form. That's easy, we use the `isLeft` API function of `Either`. So we replace
the `<Form>` element with:

```tsx
import { isLeft } from 'fp-ts/lib/Either'

[...]

 return <Form error={isLeft(validationResult)}>

   [...]
```

Nice, no conditionals yet. We can use that same construct to enable/disable the
submit button:

```tsx
        <Button primary content='Register' disabled={isLeft(validationResult)} />
```

What should happen when we click the 'Register' button? In my case, since I'm
also using Redux, it should dispatch an action that takes the `RegistrationData`
as a parameter. We can do that using `map` again. On an `Either`, map performs a
function on value if it's a `right`, and leaves it alone if it's a `left`.

```tsx
import { map } from 'fp-ts/lib/Either'

[...]

        <Button primary content='Register'
          disabled={isLeft(validationResult)}
          onClick={() => {
            map((reg: RegistrationData) => {
              dispatch(startRegistration(reg))
            })(validationResult)
          }}/>
```

The final piece of the puzzle is the list of error messages. To show that, we
want to `swap` the `Either`, meaning that it replaces left and right. That's
because we want to do something with the errors which are left, but "doing
something with an Either" mostly means doing it on a `right`. Also, we use
`getOrElse`, which is one of those safe API methods that I mentioned. Not only
does it return the `right` of an `Either`, but also do you need to specify what
has to happen when the `Either` was a `left`. Here we simply generate an empty
array in case of a left, and we have to make the TypeScript compiler happy by
saying what type that empty array has.

So we get the array of error message out of the Either and pass it into a
Semantic UI `Message` element like this:

```tsx
        <Message
          error
          header={t(
            'registration.formErrors',
            'Het formulier is niet goed ingevuld'
          )}
          list={getOrElse(constant([] as string[]))(swap(validationResult))}
        />
```

<div style="text-align: center; font-size: 3em;">🥳</div>

## Conclusion

Do you realize what we just did? We created an entry form with input validation.
It shows a full list of errors all the time, and updates it *as you type*. And
we did so **without using a single conditional** outside of the individual
simple validation functions like `oneCapital`, which is where they belong. And
as a bonus all those simple validation functions are super-easy to unit-test and
highly reusable. The other conditionals that you would normally need are now
abstracted away in the `Either` and `Validation`, so that you can't do it wrong
anymore. I don't know about you, but I'm happy. 😀

The full source code and a completely working example can be found on
[github][demo-repo]. As a bonus, it also contains a couple of Jest matchers for
checking Eithers in unit tests.

## A New Vocabulary

There is one last observation that I'd like to make. All your "C-style"
programming languages are basically the same. A loop (for, while), a condition,
some stuff about objects/classes, a switch. When you can write Java, you can
also write Swift or C#. Sure, you need to learn some new APIs, but the basic
building blocks are all the same. It's a common vocabulary shared by all these
languages, that allow you to talk and reason about code.

As I challenge myself to use the functional programming concepts more and more,
like I did in this article, I find that it gives me a whole new vocabulary in a
similar way, but on a higher abstraction level. Learn how to use `fp-ts` in
TypeScript, and you use the same constructs in Haskell, PureScript, Scala, etc.
So any investment that you make in learning this stuff is not limited to the
specific programming language that you learn it for. And it's going to allow you
to write your code using higher-level abstractions, thereby reducing hopefully
the number of bugs.

## Appendix: lift and lift2

As promised, below are the definitions for `lift` and `lift2`. The actual code
is quite simple. When you pass a validation function into it (called `check`
here), this returns a new function that, when called, first calls the original
validation function and then uses `mapLeft` to put the `left` value in an array.
It ignores `right` values. All the type stuff around it may make it look a bit
daunting, but if you give it a hard stare you should probably be able to figure
it out. 🧐

```typescript
import { pipe } from 'fp-ts/lib/pipeable'
import { Either, mapLeft } from 'fp-ts/lib/Either'
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'

/**
 * Lifts the error of a validation function into a (non-empty) array.
 * This way multiple validation functions can be composed while appending
 * their errors to the array. This variant is for a validation function
 * with one parameter.
 *
 * lift :: ( a -> Either e,b ) -> ( a -> Either [e], b )
 *
 * @param check single-arg (validation) function of which the error is lifted
 * @see lift2
 */
export function lift<E, A, B> (
  check: (a: A) => Either<E, B>
): (a: A) => Either<NonEmptyArray<E>, B> {
  return a =>
    pipe(
      check(a),
      mapLeft(e => [e])
    )
}

/**
 * Lifts the error of a validation function into a (non-empty) array.
 * This way multiple validation functions can be composed while appending
 * their errors to the array. This variant is for a validation function
 * with two parameters.
 *
 * lift :: ( a -> b -> Either e,c ) -> ( a -> b -> Either [e], c )
 *
 * @param check double-arg (validation) function of which the error is lifted
 * @see lift
 */
export function lift2<E, A, B, C> (
  check: (a: A, b: B) => Either<E, C>
): (a: A, b: B) => Either<NonEmptyArray<E>, C> {
  return (a, b) =>
    pipe(
      check(a, b),
      mapLeft(e => [e])
    )
}
```

## Making the validation functions reusable

(This section was added on June 4, 2020)

There is one thing about the code above that kept bothering me. For example, take
the validation for checking that passwords contain a number:

```typescript
export const oneNumber = (s: string): Either<string, string> =>
  /[0-9]/g.test(s) ? right(s) : left(tPasswordOneNumber)
```

The problem with this approach is that the function combines *two
responsibilities*: (1) the check whether or not the string satisfies the
conditions, and (2) attaching the proper error message when the check fails. Do
you see the problem with this? It's not reusable. Because in another form we
maybe want to use the same check, but with a different error message. We can do
better!

First, we can make a completely generic and reusable check function, which even
lives in a separate more generic file:

```typescript
import * as O from 'fp-ts/lib/Option'

export const atLeastOneNumber = O.fromPredicate((s: string) => /[0-9]/g.test(s))
```

This function returns an `Option` instead of an `Either`. An `Option` is  either
`some(value)`, or `none` (which corresponds to the value being `null` or
`undefined`). In other words: the function `atLeastOneNumber(str)` gives us
`none` when the check fails, or `some(str)` when it succeeds.

The second step is specific to the registration form validation, namely
attaching the proper error message. We do that by taking the output of
`atLeastOneNumber` and putting it in an `Either` using `fromOption`. This API
function `fromOption` converts the `some` to a `right`, and the `none` to a
`left` with the given value. So we get this (`flow` is left-to-right function
composition[^function-composition]):

```typescript
const oneNumberValidator = flow(
  atLeastOneNumber,
  fromOption(constant(tPasswordOneNumber))
)
```

The difference becomes even more apparent with the (admittedly naive) phone
number validation. Old version:

```typescript
export const phoneValid = (phone: string): Either<string, string> =>
  /^[0-9]{8}$/.test(phone) ? right(phone) : left(tInvalidPhone)
```

New version:

```typescript
const digits = (n: number) =>
  O.fromPredicate((s: string) => new RegExp(`^[0-9]{${n}}$`).test(s))

const phoneValidator = flow(
  digits(8),
  fromOption(constant(tInvalidPhone))
)
```

So now we have an even more generic and completely reusable `digits` checker,
that you can pass the desired number of digits, and a specialized
`phoneValidator` that uses it for the registration validation.

<div style="text-align: center; font-size: 3em;">🎉</div>

[either-vs-validation]: https://dev.to/gcanti/getting-started-with-fp-ts-either-vs-validation-5eja
[wat-talk]: https://www.destroyallsoftware.com/talks/wat
[fp-ts]: https://gcanti.github.io/fp-ts
[semigroup]: https://dev.to/gcanti/getting-started-with-fp-ts-semigroup-2mf7
[pipe-example]: https://gcanti.github.io/fp-ts/modules/pipeable.ts.html#pipe
[demo-repo]: https://github.com/svdo/either-validation-demo
[^exceptjs]: Except when you're using JavaScript of course, [because then basically anything goes][wat-talk].
[^lift2]: In TypeScript you can probably define `lift` in such a way that it works for both single-parameter and two-parameter functions, but that's where I draw the line for now.
[^function-composition]: So `flow` creates a new function that is the sequential combination of the functions you put into it. The call `flow(f,g)(x)` is equivalent to `g(f(x))`. You can read it as "first do `f`, then do `g`" on whatever you pass into it.
