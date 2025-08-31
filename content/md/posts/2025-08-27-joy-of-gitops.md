{:title "The Joy of Gitops for Kubernetes – Useful Tips and Best Practices"
 :subtitle "Gitops is useful for everyone, also for small companies"
 :description "When working with Kubernetes, add gitops in the mix sooner rather than later."
 :layout :post
 :tags ["kubernetes", "gitops", "Argo CD", "continuous integration", "continuous delivery"]
 :toc true
 :author "Stefan"
 :date "2025-08-27"
 :draft? true
 :unlisted? false}

 <!-- mention something about experience report in (sub)title/desc? -->

Nowadays Kubernetes seems to be the default tool used for running applications
in the cloud. Much can be said about whether that's a good idea or not, but that
is not the topic of this post. Instead, I want to talk to you about how we are
managing the workloads that are running inside our Kubernetes clusters. Setting
everything up was quite an exploration, but at this point we have a working
setup that we are very happy with. In this post I'll describe what our setup
looks like and some of the things that work well for us, as well as some thing
that you may want to avoid.

<!--more-->

## What is gitops and why would you want to use it for Kubernetes?

If you want a proper definition, please refer to your favorite search engine. My
description: gitops is a method of using git to define what is running in your
infrastructure. In other words: if you want to know what is running, you can
just look at the git repo. And if you want to change what is running, you change
it in that git repo.

Gitops can be used for any part of your infrastructure. Let's say you want to
start from scratch on a hosting provider such as OVH Cloud, Exoscale or
Scaleway, then you maybe need things like object storage buckets to exist. Maybe
you want a private network for your Kubernetes cluster. You want to define some
users, and of course the cluster itself. All of that can be done using tools
like Pulumi, Ansible, Terraform/OpenTofu, etc. And all of that can leverage
gitops. But again, that is not what this post is about; instead, this is about
using it to manage the resources running inside your Kubernetes cluster.

You probably already know some this, but for context let me start from the
beginning. Suppose I want to run an application on my Kubernetes cluster. I do
`kubectl run ...` and voila. Then the application terminates for some reason,
and things stop working. So you define a `Deployment` that manages the
application, can restart and even scale it. Since this has to be reachable from
the outside, you also want to define a `Service` and an `Ingress`. Oh and it
also needs some access to the cluster API, so you need a `Role` and
`RoleBinding`. So you run a bunch of `kubectl create` commands and all is well.
And of course, since you're a responsible person, you document the commands that
you have run somewhere. Over time things need to be adjusted, configuration is
updated, secrets are changed, and you happily `kubectl edit` all of these.
Since you're a responsible person, but also a very busy person with many
responsibilities, you sometimes forget to document the commands that you ran.
And that document is very cumbersome anyway, because you can use it to track the
_changes_, but not to see what the _expected state_ is.

For your sanity, I hope you were able to skip that stage and started at least at
the next one: instead of running all those commands, you keep a yaml description
of your Kubernetes resources in files, and track those files in git. So now when
you want to change something, you edit the file, and re-apply that file using
`kubectl apply` instead of `kubectl edit`. This gives you some advantages:

- The git repository can be used as a replacement for that document that you
  were keeping earlier; the git history shows all the _changes_ over time, and
  the latest state of the repo shows what is applied to the cluster: the
  _expected state_.
- Therefore it is now a lot easier to share the maintenance burden with your
  coworkers who were also editing resources, but obviously not as good as you in
  documenting them.

Occasionally things still go wrong. Your colleague who starts working an hour
earlier than you turned on his machine, and noticed that the application had
been crashing every half our during the night, because it ran out of memory.
Memory limits are great, but not when they are too low! All your customers are
about to start their working day, so before things spin out of control they
quickly ran `kubectl edit` to increase the memory limit. Customers happy, boss
happy, great way to start the day.

You know what happens next, right? You make an unrelated change to the
deployment in the git repo, and run `kubectl apply`, not knowing that you are
thereby resetting the memory limit to the lower value, because your coworker
didn't update the git repo.

<div style="text-align: center; font-size: 3em;" title="oh no!">😱</div>

Wouldn't it be nice if the changes that you make in your git repo were
_continuously_ and _automatically_ deployed to the cluster? And that manual
changes (`kubectl edit`) are not even possible, because the system would detect
that it no longer matches what is described in the git repo and automatically
re-apply it? Yes!

Enter gitops.

## Gitops using Argo CD

Gitops is facilitated by a tool that does the work for you, and we're using
[Argo CD][argocd][^argocd] for that. Argo CD defines the concept of an
`Application`, which groups all resources for an application: deployments,
services, ingresses, pod disruption budgets, network policies, you name it. You
store everything in a git repo, and after having Argo CD installed you tell it
you want to add that application, pointing it to your git repo. Argo CD then
checks out the repo (in the cluster, not locally) and deploys anything it finds
in that repo. It then does two things:

- It monitors whether the _actual_ state of your resources matches the _desired_
  state, namely what was defined in the git repo. If they are different, Argo CD
  will automatically reapply what was defined in the git repo, making manual
  changes impossible. Which, as I explained above, is a good thing.
- It also monitors the git repo. When it finds a new commit, it will takes that
  as its _new desired state_ updates all resources to match that new desired
  state.

## How we are using it

As soon as you're trying to use Argo CD beyond anything trivial, there's lots of
choices and design decisions that you have to make. We did that too, and I'll
tell you what we are using currently, after going through a few iterations.

### app of apps

...

### kustomize/helm/jssonet

...

### multiple envs

...

### deploy using versions.json

...

## don't / ignore (?) specify replica counts

...

## Open issues

- ... (proper continuous deployment => you don't know when _all_ apps are sync'ed)
- ...

<!-- end matter -->

[^argocd]: Another well-known alternative is [Flux][flux]. I'm making no claims
    as to which is better. A few years ago we chose Argo CD and it has suited us
    well so far. Both have pros and cons.

[argocd]: https://argo-cd.readthedocs.io/
[flux]: https://fluxcd.io
