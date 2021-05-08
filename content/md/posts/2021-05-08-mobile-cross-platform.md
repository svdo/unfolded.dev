{:title "Mobile Cross Platform Development"
 :subtitle "A New Approach"
 :description ""
 :layout :post
 :tags ["mobile", "cross platform"]
 :toc true
 :author "Stefan"
 :date "2021-05-08"
 :draft? true}

There's lots of cross-platform development frameworks for writing iOS and
Android apps. I don't like any of the existing one too much. Some I passionately
don't like, some are OK-ish, but non are _good_, I feel. I'm working on a new
approach to writing cross-platform mobile apps. I want to write a few posts
about my thinking on this topic. As always: I'm curious to learn about your
thoughts as well, so please [leave your comments][comments] and let's learn from
each other!

## Priorities

Choosing a cross-platform development toolkit is all about priorities. Are you
trying to make an app for Android and iOS as fast and cheap as possible, for
example for a marketing app? Or are you going for the best conceivable user
experience? Do you want the iOS and Android app to be as similar as possible, or
do you want them to align the the particular OS that they are running in as much
as possible? Do you think "user experience" includes performance? Matching
users' expectations?

Let's start by laying out some of my priorities. I'll talk about both sides
of this coin: user experience and developer experience.

### User Experience

- app should work the same way as the rest of the OS and other apps, so that
  users get something that feels familiar
- also think of updates to look & feel in future os versions, eg. removing force
  touch in watch os
- performance
- not "greatest common denominator"
- accessability (ref on how accessibility also improves ux for those who don't
  need it as such?)

### Developer Experience

- Framework vs Library
- myth: just one platform to learn
- small & simple enough to be able to understand the whole thing

## Architecture

- high level overview of what I'm trying to accomplish

## Conclusion

What I'm proposing is best of both: a very small UI layer that is native
Swift/Kotlin to get the best possible user experience. But also a shared
implementation of business logic, because for many parts of our mobile apps it
doesn't make any sense to implement that twice.







## Comments

Since this is a privacy-friendly static web site, I'm not including the ability
to post comments directly here. I do love feedback though, so I created a ticket
on GitHub that you can use to leave your comments. Tell me if it's bad, tell me
if it's good, but please don't forget to tell me _why_. So please head over
there and [leave your comments][comments]!


[comments]: https://github.com/svdo/unfolded.dev/issues/5
