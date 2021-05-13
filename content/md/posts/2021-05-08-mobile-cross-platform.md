{:title "ReactNative, Flutter, Cordova, etc: the state of the art is not good enough."
 :subtitle "A new approach to developing cross-platform Android and iOS apps"
 :description "There's lots of cross-platform development frameworks for writing iOS and Android apps. I don't like any of the existing ones too much. Some I passionately don't like, some are OK-ish, but none are great, I feel. In this first post of a series I describe what I think the problems are, and the direction I'm taking to address them."
 :layout :post
 :tags ["mobile", "cross platform", "Strohm Native"]
 :toc true
 :author "Stefan"
 :date "2021-05-09"
 :draft? false}

There's lots of cross-platform development frameworks for writing iOS and
Android apps. I don't like any of the existing ones too much. Some I
passionately don't like, some are OK-ish, but none are _good_, I feel. I'm
working on a new approach to writing cross-platform mobile apps. I want to write
a few posts about my thinking on this topic. And remember: I'm curious to learn
about your thoughts as well, so please [leave your comments][comments] and let's
learn from each other! Important note: I'm explicitly excluding games and the
like from the scope. I'm not in that business, and in that business completely
different reasoning applies. For lack of a better term, let's say I'm talking
about "business apps".

## Priorities

Choosing the right cross-platform development toolkit is all about priorities.
Are you trying to make an app for Android and iOS as fast and cheap as possible,
for example for a marketing app? Or are you going for an awesome user
experience? Do you want the iOS and Android app to be as similar as possible, or
do you want them to align the the particular OS that they are running in as much
as possible? Do you think "user experience" includes performance? Matching
users' expectations? And of course: what skills do you(r developers) have?

Let's start by laying out some of my priorities. I'll talk about both sides
of this coin: user experience and developer experience.

### User Experience

To me it makes no business sense to want an iOS and Android app to look and feel
identical. Except maybe when you are optimizing for development cost at the
expense of everything else. And that is not the market that I want to be in. A
good user experience means that the user interface "disappears". Users can do
the task that they want to do without the user interface ever getting in their
way. I don't want my users to wonder "how do I get this done?", or "what will
happen when I touch that thing on the screen?" I want them to just know, even if
they never saw the screen before. The only way you can do that, is by **making
sure everything in your app works exactly the same way as the rest of the
platform**, be it iOS or Android. Since these platforms have different look and
feel and interaction styles, the Android and iOS version of any (non-trivial)
app must by definition be different.

There is a more subtle aspect to this as well: changes **from one operating
system version to the next**. If Apple or Google decide to change how certain
gestures work in standard UI controls, I want my app to match those changes
automatically, without redeploying my app. Using the native components can take
care of that. An example of this situation is when Apple removed force touch in
WatchOS. Properly developed apps with native UIs didn't need to be redeployed,
they automatically adopted the new interaction mechanism.

Also, something that naturally happens with cross-platform frameworks, is that
the **feature set of an app** becomes the "lowest common denominator" of what is
provided by the framework for both operating systems. Maybe for my app it makes
sense to provide a custom today extension for iOS, whereas on Android it makes
more sense to spend more time on rich notifications, I don't know. The point is:
I don't want to be limited by the framework. And yes, I know, most of these
frameworks allow you to break out and implement custom platform-specific parts,
but that defeats the purpose, doesn't it? (More on this below.)

Another point is **accessibility**: cross-platform frameworks tend to support only
parts of the accessibility features of the native environments, so that it
becomes very hard to support your users that have extra needs. And using native
SDKs you already get a lot out of the box, e.g. when using SwiftUI your
interfaces automatically support dark mode and dynamic type, to name a few. And
make no mistake: there is lots of research that suggests that "regular" users
also benefit from accessibility-related improvements.

Finally, in this section, I don't want to say "**performance**", but there you go.
Good performance is also part of good user experience. To be fair, most
cross-platform frameworks have very good performance nowadays, in most cases
(which is not the same as "all cases", and the 80/20 rule tends to apply).

### Developer Experience

So how many platforms do you / your team have to learn exactly when using your
favorite cross-platform framework? If, for a non-trivial app, your answer is
"one", then I will have a very hard time believing you. These frameworks are
necessarily full of [leaky abstractions][wikipedia-leaky-abstraction]. At the
end of the day there always comes a point where you have to understand how the
native side of things works as well. And it's never the easy things that you
need to implement natively... This is always the argument, right? "With
ReactNative [insert your own framework here] you only have to learn one
platform." Well, you're going to be disappointed. Instead of needing deep
understanding of two platforms (Android and iOS), now **you need three**! Have
you ever met anyone who has deep understanding of ReactNative, Android _and_
iOS? Those people are very, very rare.

Then there's the good old "framework vs library" discussion. Since these terms
are somewhat overloaded, let me quote Wikipedia for some definitions:

> In computer science, a **library** is a collection of non-volatile resources used
> by computer programs, often for software development. These may include
> configuration data, documentation, help data, message templates, pre-written
> code and subroutines, classes, values or type specifications.
> [[source]][wikipedia-library]

In contrast:

> In computer programming, a software **framework** is an abstraction in which
> software providing generic functionality can be selectively changed by
> additional user-written code, thus providing application-specific software. It
> provides a standard way to build and deploy applications and is a universal,
> reusable software environment that provides particular functionality as part
> of a larger software platform to facilitate the development of software
> applications, products and solutions. [[source]][wikipedia-framework]

The difference is **where the control is**. With a library, your app is in
control and can use components from libraries (or not). When using a framework
it's the other way around. The framework is the basis of your app, and you have
to use the means that are provided by the framework to get your stuff done. If
your desires match those of the framework only in part, you're out of luck. From
my own experience, when I have to use a framework I invariably run into the
boundaries that it sets. The customer (or myself) wants something that simply
isn't possible with the framework unless spending tons of effort (if then). Most
cross-platform tools are frameworks. I don't like frameworks. They make my
software more expensive, because sooner or later you always run into those
boundaries.

I want to have a library that is powerful yet small enough so that you, as a
developer building an app with my library, can **understand the thing in its
entirety** if you need to. Of course it's my goal that you don't need to most of
the time.

## Architecture

Here is the idea that I'm implementing. I call it "**Strohm Native**"[^strohm].
It implements the flow architecture (as also implemented by Redux, for example),
on the boundary between native Swift/Kotlin and JavaScript. The store, reducers
and actions are in JavaScript, but the subscriptions to changes of the store are
done on the native side. Using data binding, you can then easily bind specific
parts of your store's state to your native UI. If you don't know what this is
all about, no worries, that's ok. I will explain in an upcoming post.

The idea is that (1) you create a view model on the native side, where you
subscribe to the parts of the state that you're interested in:

![Strohm: Subscribe](/img/strohm-subscribe.jpg)

On the cross-platform side (2) you implement your reducers. Then (3) you
dispatch actions from the native view, and automatically receive updated state
in your view model. Strohm supports data binding, so you can have it in such a
way that the UI is automatically updated whenever the state changes.

![Strohm: Redux](/img/strohm-redux.jpg)

The common logic (as in: the logic shared between Android and iOS) is based on
JavaScript, but of course you can implement that in **any language that compiles
to JavaScript**. It shouldn't be too difficult to add support for other
languages, but for now I'm focussing on ClojureScript.

## Conclusion

What I'm proposing is best of both: a very small UI layer that is native
Swift/Kotlin to get the best possible user experience. But also a shared
implementation of business logic, because for many parts of our mobile apps it
doesn't make any sense to implement that twice.

I know this is not revolutionary. But I do think that it wasn't this easy to
have native views with cross platform business logic before. Without more
technical details it's probably too early to ask, but I'm very curious whether
this approach appeals to you and whether you might see yourself using it some
day. I'll be posting more about the technical details soon!

## Comments

Since this is a privacy-friendly static web site, I'm not including the ability
to post comments directly here. I do love feedback though, so I created a ticket
on GitHub that you can use to leave your comments. Tell me if it's bad, tell me
if it's good, but please don't forget to tell me _why_. So please head over
there and [leave your comments][comments]!

[^strohm]: I named it "Strohm", inspired by how the name "Drupal" [came to
be](https://www.drupal.org/about/history). The Dutch word "stroom" means "flow",
and the English pronounciation of that word is very close to "strohm".

[comments]: https://github.com/svdo/unfolded.dev/issues/5
[wikipedia-library]: https://en.wikipedia.org/wiki/Library_(computing)
[wikipedia-framework]: https://en.wikipedia.org/wiki/Software_framework
[wikipedia-leaky-abstraction]: https://en.wikipedia.org/wiki/Leaky_abstraction
