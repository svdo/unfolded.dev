{:title "The Joy of Gitops for Kubernetes – Useful Tips and Best Practices"
 :subtitle "Gitops is useful for everyone, also for small companies"
 :description "When working with Kubernetes, add gitops in the mix sooner rather than later."
 :layout :post
 :tags ["kubernetes", "gitops", "Argo CD", "continuous integration", "continuous delivery"]
 :toc true
 :author "Stefan"
 :date "2025-08-27"
 :draft? false
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
gitops. But that is not what this post is about; instead, this is about
using gitops to manage the resources running inside your Kubernetes cluster.

### Kubernetes Level 1: manually editing resource definitions

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

### Kubernetes Level 2: manually applying resource definition files

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
thereby reverting the memory limit to the lower value, because your coworker
didn't update the git repo.

<div style="text-align: center; font-size: 3em;" title="oh no!">😱</div>

Wouldn't it be nice if the changes that you make in your git repo were
_continuously_ and _automatically_ deployed to the cluster? And that manual
changes (`kubectl edit`) are not even possible, because the system would detect
that it no longer matches what is described in the git repo and automatically
re-apply it? Yes!

Enter gitops.

### Kubernetes Level 3: Gitops

The crucial difference when you're using gitops, is that you basically _cannot_
manually change resources anymore. Inside the Kubernetes cluster, some
controller is running that continuously pulls your gitops repo, and
automatically makes sure that all _live_ resources are the same as the _defined_
resources in the gitops repo. So if you edit something manually, it will just be
automatically undone by the gitops controller.

Going back to the example before, it means that your colleague comes in early in
the morning, detects the problem, but now has no other way of fixing it than by
pushing an update of the resource definition in the gitops repo. The gitops
controller automatically pulls the updated repo and applies the change. Problem
solved, but now the gitops repo still matches the live version, so you can
safely make your own changes without worrying about discrepancies with the
cluster state.

## Upcoming Posts

At Viduet, when we started using Kubernetes, we also immediately did so using
gitops. It has been a very rewarding experience, even though things get a bit
more complex now and then. This post is the first one in a series of posts, in
which I describe the setup we have deployed, using kustomize/helm/jsonnet,
managing multiple environments in one repository, integration with CI/CD, and
lessons learned. If there is anything you are specifically interested in, please
let me know at stefan _at_ viduet.eu.

Stay tuned!

<div style="text-align: center; font-size: 3em;" title="Stay tuned!">📻</div>

<!-- end matter -->
