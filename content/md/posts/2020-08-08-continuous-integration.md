{:title "Continuous Integration: What It Is and Four Reasons Why I Like It"
 :subtitle "Software Development: Easy To Learn, Hard To Master"
 :description "Right after creating a new project, for may of those I'll also setup continuous integration fairly quickly. In this post I'll go into what I mean with 'continuous integration' and the reasons why I like it: having a safety net, lowering the threshold for third-party contributions, having executable documentation, and making sure that things will keep working in the future."
 :layout :post
 :tags ["continuous integration", "continuous delivery", "ci/cd", "software quality"]
 :toc true
 :author "Stefan"
 :date "2020-08-08"}

_In my daily work, I have the good fortune to work with many very talented
people. Some are professional software developers, like myself. Others are for
example researchers, scientist, project leaders, and so on. Is that you? Do you
also write software, but not as your main activity? Do you have some experience
but maybe no formal training? In [this series of blog
posts](/pages/software-development-easy-to-learn-hard-to-master/) I will explain
the best practices that I use, so that you may be able to benefit from them. So
that writing software hopefully takes you less effort and gives you more
pleasure._

## The second thing for new code

The very first thing I _always_ do when starting something new, is `git init`.
Potential new hobby project? `git init`. Sample code for a blog post? `git
init`. Doing programming exercises on something like Exercism.io? `git init`.
And then I start using git the way I described in the [first
post](/posts/2020-07-21-using-git-more-effectively) of this series. There are
virtually no exceptions to this; it hardly costs me anything and it has helped
me plenty of times.

The second thing that I do, I do just slightly less often, but still very often:
set up _continuous integration_. Obviously I won't do that for things that I
never push to a remote. And some things that I push to a remote don't need it
either, for example those Exercism coding exercises. But for anything that I
suspect may have a longer life than a few weeks, including [for that sample code
for a blog post][either-validation-ci], I'll set it up.

## What is continuous integration

To get rid of potential confusion, let me explain what I mean with continuous
integration, or _CI_ for short. In its most basic form, it's actually quite
simple: every single time you push something to your git remote (e.g. GitHub,
BitBucket, GitLab, etc), an automated process will start that builds your code
and runs all your tests. (You do have unit tests, right? Maybe a nice topic for
a future post in this series.) That's all, if you do that you can claim you're
doing continuous integration.

The other day, I was talking to one of my researcher colleagues about coding
practices. When I asked him whether he was using continuous integration, he
thankfully asked for clarification. For me the term has become so commonplace
that I hadn't realize that of course for him it wasn't. He is working on a
software library that his (internal) customers integrate into their product. So
quite logically he thought that was what I meant: continuously automatically
integrating his releases into the customer's product code. While doing that is
of course fantastic, that's going quite a few steps further than just basic CI.
In fact, I would say that that is the combination of _continuous delivery_ (the
_CD_ in _CI/CD_) on his part, plus _continuous integration_ of his delivery on
his customer's end.

So if you have a [Travis job][travis-tutorial], a [CircleCI
pipeline][circle-ci-getting-started], a [GitLab
pipeline][gitlab-pipelines-getting-started], a [GitHub Actions
workflow][github-actions], a [Bamboo build][bamboo], anything that automatically
build and tests your code when you push it to your remote, you're doing
continuous integration.

<div style="text-align: center; font-size: 3em;" title="relieved">😌</div>

## The value of continuous integration

Ok, so now that we're hopefully on the same page regarding the term _continuous
integration_, you may be curious why you should bother. Here's a few reasons
that I have.

### Safety net / quality baseline

I'm human. I make mistakes. There, I've said it, now you know. I have learnt
that mistakes that escape the development process and end up at the customer,
are way more expensive than mistakes that are caught earlier. So that's why I
like pair programming, because your pair can catch your mistakes while your
making them. It doesn't get any cheaper than that. That's also why I like unit
tests, because they run many times an hour so they also catch mistakes very
quickly. And that's also why I like continuous integration, because it catches
even more mistakes before they escape the development process. For instance,
when I forget to run my unit tests before pushing my code to the remote: CI will
run and immediately notify me. And my reaction to that notification is: cool,
thanks for catching that!

<div style="text-align: center; font-size: 3em;" title="oops">🤭</div>

### Makes it easier for others to contribute

CI runs for any code that is pushed to the remote, not just by me/my team. So if
it's source code that others can also see and use, having CI will lower the
threshold for them to contribute. Even when you have a new team member it will
help. They don't know the source code as well as you do, so they are a lot more
likely to make mistakes. When I contribute code to somebody else's project, it
feels very comforting to see "all green" as part of the automated tests in the
pull request!

<div style="text-align: center; font-size: 3em;" title="all is ok">✅</div>

### Executable documentation of build & test (& deployment)

Just like unit tests are a form of executable code documentation, CI is a form
of executable documentation of how your project can be built and tested. And, if
you're doing CI/CD, deployed. And I don't think I need to explain the benefits
of having executable documentation over plain only-human-readable documentation,
or do I?[^letmeknow]

### Ensure that code keeps working in the future

The final reason I'll mention here, is the future. Because there are two
possible sources of problems: you as part of the development team pushing code,
but also the environment changes. Libraries that you depend on may turn out to
contain security issues[^dependabot], or cease to exist altogether. Operating
systems are updated, as well as libraries in there that you may directly or
indirectly depend on. If you have a CI script that periodically runs, even when
you don't push anything, you will know that your code not only works as
specified when you push it, but also in the future!

<div style="text-align: center; font-size: 3em;" title="things break">🦾</div>

## Some encouraging words

When I'm setting up CI, which I have done countless times, it's always a
challenge. Every single time I have to brace myself and mentally prepare for
doing quite a few tries before getting it right. It's just such a complex black
box! You write a script that will run on a not-quite-known configuration on a
unknown server somewhere, and if it fails you get limited feedback. For example,
if you want to automatically test an iOS app, you'll have to specify which
version of the iOS simulator should be used. But before trying the build, you
may not know which are available and what the simulator device identifiers
are that you can use. So you have to try, look at the output, and try again. And
then something else isn't quite right, and you try again. And again. And once
more. Just have a look at the [eighteen (18!) commits][ci-commits] I did for
sample code for a blog post. And you can't even really clean up those commits
because you _have_ to push them to the remote. I guess if it's important to you,
you can do them on a separate branch and squash-merge the branch when you are
done, but I often don't bother. But bottom line: if it doesn't work immediately
and takes a lot of attempts, it's not you. You're not losing your touch, it's
not because you're getting older. You're fine, it's just complicated.

<div style="text-align: center; font-size: 3em;" title="hug">🤗</div>

And when you finally have that working CI build and it passes, go ahead and slap that badge on your readme!

<p style="text-align: center"><img src="/img/ci-passing.png" alt="CI passing" width="90" /></p>

## [Comments][comments]

Since this is a privacy-friendly static web site, I'm not including the ability
to post comments directly here. I do love feedback though, so I created a ticket
on GitHub that you can use to leave your comments. Tell me if it's bad, tell me
if it's good, but please don't forget to tell me _why_. So please head over
there and [leave your comments][comments]!

[either-validation-ci]: https://github.com/svdo/either-validation-demo/actions
[travis-tutorial]: https://docs.travis-ci.com/user/tutorial/
[circle-ci-getting-started]: https://circleci.com/docs/2.0/getting-started/#section=getting-started
[gitlab-pipelines-getting-started]: https://docs.gitlab.com/ee/ci/quick_start/
[github-actions]: https://docs.github.com/en/actions/getting-started-with-github-actions
[bamboo]: https://confluence.atlassian.com/bamboo/getting-started-with-bamboo-289277283.html
[ci-commits]: https://github.com/svdo/either-validation-demo/search?q=%5Bci%5D&type=Commits
[dependabot-search]: https://github.com/search?q=dependabot&type=Commits
[comments]: https://github.com/svdo/unfolded.dev/issues/3
[^letmeknow]: Please let me know if I should explain by [leaving a comment][comments]!
[^dependabot]: A [quick search on GitHub][dependabot-search] shows that there are currently over 6.5 million commits containing the word "dependabot", which is GitHub's automatic security-issue bot.
