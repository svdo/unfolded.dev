{:title "Using Git as a Logbook to Improve Efficiency"
 :subtitle "Software Development: Easy To Learn, Hard To Master"
 :layout :post
 :tags  ["git", "version control", "xp"]
 :toc true
 :author "Stefan"
 :date "2020-07-21"}

*In my daily work, I have the good fortune to work with many very talented
people. Some are professional software developers, like myself. Others are for
example researchers, scientist. They typically also write software, but not as
their main activity. Also, they often have no formal training as software
developers. In [this series of blog
posts](/pages/software-development-easy-to-learn-hard-to-master/) I will explain
the best practices that we use as professional software developers, so that
others may be able to benefit from it. So that writing software hopefully takes
them less effort and gives them more pleasure.*

## A Creative Pursuit

A decade or so ago, I had the good fortune to work with a group of extremely
smart scientists. Without going into details, their job was to conceive
algorithms for image processing, and improve them. They had huge volumes of
data, and the ability to create more. The algorithms had to be robust in the
face of all kinds of variations in that data. Much like software development,
their work was [a creative pursuit][creative-pursuit].

They would have regular meetings to discuss progress, results and challenges. In
those meetings, I remember being surprised now and then. For example, you might
hear a conversation like this[^fiction]:

> **Betty**: Say, John, didn't you have some progress on &laquo;some algorithm
> feature&raquo; a few weeks ago? I'm running into some issues and I think your
> findings may help me here.
>
> **John**: Yeah, that's right, I remember having some results when I was
> working on &laquo;something else&raquo;. I'm not sure which algorithm variant
> I used exactly though, or which test data for that matter. If it's important
> to you, we can sit together and see if we can reproduce that.

While this was commendable in terms of team spirit, I always felt it wasteful
that apparently things had not been tracked and documented in such a way that it
was easy to recover what people had tried and done.

<div style="text-align: center; font-size: 3em;">🤔</div>

## Not A Unique Problem

It turns out that software development is not that different from research in
the sense that they are both creative pursuits. Also software development
requires a good amount of trial-and-error, backtracking, rethinking, trying
again, going back to something that wasn't so stupid after all. The difference
is that we, software developers, are lazy to the point that we try to automate
just about anything that we can. So also for this we use tools. And yes,
nowadays probably everybody understands that I'm going to say "git" (or
"mercurial" or something similar)[^vcs-history]. So I'm not going to explain
what git is here. Plenty of good resources out there on that topic. No, I'm
going to tell you a bit about how I use it.

<div style="text-align: center; font-size: 3em;">🛠</div>

## A Logbook On Steroids

My version control system (e.g. git) is like a logbook. I use it to record
relevant things about my work that are not part of the code itself. For example,
in a commit message I type the reason why I'm doing things a certain way. Or I
might record that I tried something but it failed. Let's go into some details to
better explain.

### Target Audience

For any kind of writing, an important question is: who is your target audience?
Whether it's a blog post like this one, a scientific paper, a conference talk,
you always ask yourself: for whom am I writing this? For version control, the
answer is:

1. My future self. Much like the researchers in the example above, I cannot hold
   everything in my head. I forget details, I forget why I tried certain things,
   I forget why other things failed. By documenting them, I can remind myself
   when I need to in the near or not-so-near future.
2. My team. Even though I very much like pair programming, and I hope one day to
   be part of a team that wants to do [mob programming][mob-programming], in
   reality I cannot share all relevant details with the rest of my team all the
   time. By documenting them, I allow my team mates to step into my shoes and
   follow my reasoning.
3. Successors or other people who may have to work on the code when I'm not
   around. Even though the average "best before" period of software is a fair
   bit shorter than people prefer to believe, it still happens that I write code
   that others need to continue working on when I'm not available, for example
   because I'm in a different department or working for a different employer.
   Again, by documenting things I try to enable them to decide what pieces are
   important to them and which are not.

### Tell A Story

Well, maybe that's a bit overdoing it. My kids are going to be bored out of
their socks by these stories. But still, I do try to commit in such a way that
the sequence of all commit messages can be understood by others. They say more
than the individual commits in isolation, because the succession of commits
allows you to distill my reasoning, from where to where I'm going.

### Record Frequently

This requires to commit frequently. "Oh but I do! I commit at least every day
before I leave the office! And sometimes even more!", you say? Well, we're
thinking of a different order of magnitude then. When I'm "[in flow][flow]" I
commit *many times every hour*. Every few minutes when I'm really on a roll. No,
of course not always. When I'm grinding on a hard problem I may have nothing to
commit for a few hours. That's ok, but it should be exceptional, not the norm.

Committing frequently also means that I make separate commits when reasonably
possible. For example, as I'm writing this blog post, I'm making a few changes
to the CSS style sheet. I could commit those as part of the blog post, or as a
separate commit. Instead, I chose to create
[three][commit1]&#32;[separate][commit2]&#32;[commits][commit3] because each
makes sense on its own, without the other two. This way I can later revert any
of the three changes by simply reverting the corresponding commit. Of course
some people find this rather extreme[^xp], and that's fine. YMMV.

### Rewrite

And like any good story, also this one needs to be polished and rewritten to be
as good as it can be. I do that all the time. When I'm working on a feature, I
may for example have ten commits that form a logical whole. Then I notice
that there was a change that should have been part of one of those commits, but
I overlooked it at that time. So I make a new commit with that single thing that
I forgot, I move that new "fixup" commit to the commit where it belongs, and I
combine the two. I look at the commit messages and edit them if needed.
I reorder commits. And then, when I'm satisfied, only then I push my commits to
the remote, so that others will only see the polished version of my story.
Obviously having a great git client tremendously helps with this. I can't
recommend anything specific on Linux or Windows, but on macOS [GitUp][gitup] is
extraordinary in this regard: hitting "d" moves a commit down, hitting "f" does
a fixup (combine it with the commit before it), "e" edits the commit message. I
don't know of another git client that can do this so easily.

### Record Failures

One final thing to note here: don't hesitate to also record failures. Most of
the time there is more to be learned from failures than from successes. So when
rewriting my commits before pushing, I may either delete a few commits that were
failures, but sometimes I actually decide to leave them there and explain (in
the commit messages) why. Same for "reverse commit". I try something that
doesn't work but is already committed. I may then delete that last commit before
pushing, but I can also "reverse commit" it. Again, so that it may be clear that
I tried something and that it failed, so that others don't have to try
themselves as well.

<div style="text-align: center; font-size: 3em;">📔</div>

## When To Use It

In closing, I want to say a few words about *when* I use a version control
system. The answer may surprise you, because it is: "almost always". Yesterday
and today, I was trying out a few different crypto libraries for some finite
field arithmetic that I needed to do. So I created a few small projects to
quickly try a few libraries. And for each of those, I created a git repo. Just
on my own computer, mind you. Unless I end up creating a spike that I think has
value to others, I won't create a remote for these repos. But just typing `git
init` after creating a new project doesn't cost me anything. When I don't need
it anymore, I remove the project, and the embedded `.git` folder with the
history is automatically deleted as well. So why not?

Like Woody Zuill says it: "turn up the good!" If using a version control system
like git is good, turn it up. Use it more than you used to, and then more still,
and see what happens. If committing to git is good, turn it up. Commit more
frequently, and then more frequently still. Maybe you'll be surprised. I hope it
will bring you the satisfaction it has given me!

<div style="text-align: center; font-size: 3em;">🤠</div>


[creative-pursuit]: https://iism.org/article/why-are-ceos-failing-software-engineers-56
[corecursive]: https://corecursive.com/software-that-doesnt-suck-with-jim-blandy/
[mob-programming]: https://040code.github.io/2019/03/15/mob-programming
[flow]: https://en.wikipedia.org/wiki/Flow_(psychology)
[gitup]: https://gitup.co
[commit1]: https://github.com/svdo/unfolded.dev/commit/3fec2de430168ad50cbdee7144b5f5f827321be4
[commit2]: https://github.com/svdo/unfolded.dev/commit/63c1a75a857b8c6075c9d8302535309b1857ec37
[commit3]: https://github.com/svdo/unfolded.dev/commit/9fb7189a19b5d1b0f53113996d9a95259395b193
[xp]: http://www.extremeprogramming.org
[^fiction]: The names and conversation is made-up, but not unlike real conversations that I have witnessed.
[^vcs-history]: In the time that I meant in the beginning, git didn't exist yet. Even subversion was not that common yet. CVS anyone? RCS? Listen to [this episode of the amazing CoRecursive podcast][corecursive] if you're interested in a bit of history about CVS, SVN and git.
[^xp]: You may not be surprised to hear that I'm fond of [Extreme Programming][xp].
