{:title "Continuous Insights: Graphs And Metrics For Free"
 :subtitle "Simple method for gaining CI insights for private repos and commercial use – for free"
 :description "It is often useful to collect some metrics from your continuous integration setup, so that you can track them over time as you are evolving your source code. For example, maybe you want to have a graph of your code coverage. Or of the runtime performance of your system. This post tells you how I set up a very simple solution that requires nothing that you don't already have: no extra subscriptions, no third-party services."
 :layout :post
 :tags ["continuous integration", "insights", "metrics", "rapid feedback"]
 :toc true
 :author "Stefan"
 :date "2020-10-28"}

In my [previous post about continuous
integration](/posts/2020-08-08-continuous-integration) I explained that I always
quickly setup continuous integration for any project. In that post I explained
that it gives safety, allows for easier collaboration, and gives more confidence
that things keep working the way they should.

In this post I'll tell you about a nice trick that I have used
a few times to get insights from your continuous integration. There are many
third-party services that facilitate this of course, and they do so in a much
nicer way than I'll be describing here. But the thing is: you're not allowed to
use most of those for commercial purposes or private repositories. And sometimes
you just don't have the money to spend on things like this. So read on if you
want to know how you can easily gain insights with all the tools that you
already have, for free.

## The end result

Let's start by showing an example of what we're going for. Below is a graph that
shows two code coverage measurements over time. Each data point represents a
build on the CI server. The graph is interactive and the CI server automatically
causes the graph to be updated. After you have set everything up, there are no
manual steps anymore.

![Coverage Graph](/img/coverage-graph.png)

## How does it work

The solution leverages the ability to publish "pages" that most collaboration
platforms have nowadays (e.g. [GitHub][github-pages],
[BitBucket][bitbucket-pages], [GitLab][gitlab-pages]). This is basically a
static web site that is hosted by the platform, using the content of a special
git repository[^or-branch] that you choose for that purpose. The CI system
checks out this repository, updates the file containing the measurements, and
commits and pushes it back to the repo. In a picture:

![Sequence Diagram](/img/ci-metrics-sequence.jpg)

1. You push to your source code repository, which triggers the CI server.
2. The CI server builds your project (like before).
3. The CI server takes the desired measurements, e.g. performance, coverage, etc.
4. The CI server clones and checks out the repo where you published the graphs.
5. The CI server updates the measurements file in that repository.
6. The CI server commits the update and pushes it back to the pages repo.
7. The pages repository gets deployed (this is done automatically by your
   collaboration platform).

## Details and source code

Here come the details. In my description below, I'll assume that you're using
CircleCI as your continuous integration tool, and BitBucket as your
collaboration platform. Other choices are perfectly fine as well and the
solution very similar; I'll leave the required changes needed for that as an
exercise to the reader.

<div style="text-align: center; font-size: 3em;" title="smarty pants">🤓</div>

###  Pages repo and empty measurements file

First step is to create your pages repository. Go ahead and create it. Then
clone it and add the empty file that we'll be storing the measurements in:

```bash
git clone git@bitbucket.org:my-org/my-pages-repo.bitbucket.io.git
cd my-pages-repo.bitbucket.io
mkdir my-awesome-project
touch my-awesome-project/stats.txt
git add my-awesome-project/stats.txt
git commit -m 'Creates empty measurements file for my-awesome-project'
git push
```

When all is well and correctly configured, BitBucket should now publish this
pages repository and you should be able to load the empty stats file in your
browser under `https://my-pages-repo.bitbucket.io/my-awesome-project/stats.txt`.

### A custom reporter to update the measurements file

In my case, as I'm using [Clojure][clojure] and [Leiningen][leiningen]. I'm
using [Cloverage][cloverage] to measure and report code coverage. I need to
process the coverage data and write it out in my desired format, so I'll use a
custom coverage reporter for that. Alternatively you might create a script that
runs your coverage tool and parses its output.

```clojure
(ns coverage-stats
  (:import
   [java.io File]
   [java.time Instant])
  (:require
   [clojure.string :as str]
   [cloverage.report :refer [file-stats]]))

;; This custom reporter can be used with Cloverage:
;;
;; ```
;; lein cloverage -c coverage-stats/report
;; ```

(defn summary [^File forms]
  {:post [(= 3 (count %))]}
  (let [stats (file-stats forms)
        data (mapv (juxt :forms :covered-forms :instrd-lines :covered-lines :partial-lines) stats)
        [total-forms total-covered-forms total covered partial] (apply mapv + data)]
    {:timestamp     (.toEpochMilli (Instant/now))
     :form-coverage (float (* 100 (/ total-covered-forms total-forms)))
     :line-coverage (float (* 100 (/ (+ covered partial) total)))}))

(defn report [{:keys [forms ^String output]}]
  (let [filename (str/join "/" [output "stats.txt"])
        existing-stats (slurp filename)
        {:keys [timestamp form-coverage line-coverage]} (summary forms)]
    (println "Writing stats:" filename)
    (spit filename (str existing-stats
                        (str/join "\t" [(str timestamp)
                                        (format "%.1f" form-coverage)
                                        (format "%.1f" line-coverage)])
                        "\n"))))
```

What this reporter does is basically:

* Read the contents of `stats.txt`
* Add a line in the format:<br />
  `[timestamp]<TAB>[form-coverage]<TAB>[line-coverage]`
* Write the updated contents back to `stats.txt`

### Collect measurements

Now I can use this reporter to compute code coverage and update `stats.txt`. You
can test this in your dev setup:

```bash
lein with-profile dev,devsrc cloverage --no-html -c coverage-stats/report

```

### Retrieve, update and store measurement data

Now I put this command in a script that does all the hard work. This is what it
looks like, you of course need to modify it for your specific circumstances. I
call this script `report-coverage.sh` and it lives in the root of my source code
repo.

```bash
#!/bin/bash

# Make sure script fails when command before pipe fails
set -o pipefail

# Make sure script fails on any error
set -e

currentBranch=$(git rev-parse --abbrev-ref HEAD)
if [ ${currentBranch} != "master" ]; then
    echo "Current branch is ${currentBranch}."
    echo "Coverage reporting is only done on master. Exiting."
    exit
fi

# Clone stats repo and get existing stats file
(
  mkdir -p target/coverage
  cd target/coverage
  git clone git@bitbucket.org:my-org/my-pages-repo.bitbucket.io.git
  cd my-pages-repo.bitbucket.io
  git config --local user.name 'Continuous Integration'
  git config --local user.email ci@my-org.com
  cp my-awesome-project/stats.txt ..
)

# Run coverage command; this updates `stats.txt`
lein cloverage --no-html -c coverage-stats/report

(
  # Copy updates stats file to repo clone
  cd target/coverage
  cp stats.txt my-pages-repo.bitbucket.io/my-awesome-project

  # commit new version
  cd my-pages-repo.bitbucket.io
  git add my-awesome-project/stats.txt
  git commit -m 'Updates my-awesome-project coverage stats'
  git push
)
```

### Add access keys

For the above to work, you need to allow CircleCI access to push to your
BitBucket repo. In CircleCI I added a "user key" to my project's settings. Then
in BitBucket I added the public key as an SSH key to my organization. The
fingerprint of the key (as displayed by CircleCI) I added to my build's
`config.yml` (see [CircleCI docs][circleci-add-keys]).

### Activate the report-coverage.sh script

This should be enough to activate the collecting of measurements. Add a call to
the `report-coverage.sh` script in your `config.yml` and see whether it works.
And then, [CI being CI](/posts/2020-08-08-continuous-integration#some-encouraging-words), fix things, retry, fix, retry, etc until it works.

<div style="text-align: center; font-size: 3em;" title="fingers crossed">🤞</div>

### Visualize

Now for the final part: create a nice visualization. In your pages repository,
the one that you created above and where the `stats.txt` file lives, you add an
HTML file that will show a graph. Let's call it `insights.html`. I'm using the
following code for my visualization, but you can of course use any method you
like.

```html
<html>

<head>
  <script src="https://code.jquery.com/jquery-3.5.1.min.js"
    integrity="sha256-9/aliU8dGd2tb6OSsuzixeV4y/faTqgFtohetphbbj0=" crossorigin="anonymous"></script>
  <script src="https://cdn.jsdelivr.net/npm/echarts@4.9.0/dist/echarts-en.common.min.js"></script>
  <script type="text/javascript">
    $(document).ready(function () {
      jQuery.get('https://my-pages-repo.bitbucket.io/my-awesome-project/stats.txt', function (data) {
        var myChart = echarts.init(document.getElementById('main'))
        var parsedData = data.split('\n')
          .filter(line => line.length > 0)
          .map(line => line.split('\t'))
          .map(line => [
            new Date(parseInt(line[0])),
            parseFloat(line[1]),
            parseFloat(line[2])
          ])
        var forms = parsedData.map(entry => [entry[0], entry[1]])
        var lines = parsedData.map(entry => [entry[0], entry[2]])
        var option = {
          xAxis: {
            type: 'time',
            splitLine: { show: false },
            axisLabel: {
              formatter: function (value, index) {
                var date = new Date(value)
                const options = { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }
                const formatter = new Intl.DateTimeFormat(undefined, options).format;
                return formatter(date)
              }
            }
          },
          yAxis: { type: 'value', min: 0, max: 100 },
          legend: { data: ['forms', 'lines'] },
          series: [
            { name: 'forms', type: 'line', data: forms },
            { name: 'lines', type: 'line', data: lines }
          ],
          dataZoom: [
            { type: 'inside', xAxisIndex: [0] },
            { type: 'slider', xAxisIndex: [0] }
          ],
          tooltip: {
            trigger: 'axis',
            formatter: function (params) {
              var date = new Date(params[0].value[0]);
              var forms = params[0].value[1]
              var lines = params[1].value[1]
              return date.getDate() + '-' + (date.getMonth() + 1) + '-' + date.getFullYear() + ' : ' + forms + '% forms, ' + lines + '% lines';
            },
            axisPointer: {
              animation: false
            }
          }
        }
        myChart.setOption(option)
      })
    })
  </script>
</head>

<body>
  <h1>Graph</h1>
  <div id="main" style="width: 600px;height:400px;"></div>
</body>

</html>
```

Done! Go to
`https://my-pages-repo.bitbucket.io/my-awesome-project/insights.html` and admire
the result.

<div style="text-align: center; font-size: 3em;" title="Tada!">🎉</div>

## Warning: pages are public

One important warning: the metrics pages and graphs that you create this way are
public on most platforms. You can of course "obscure" things by requiring people
to know the exact URL to your graphs, and by being careful about what
information you put in there. But requiring things like username/password is not
possible using this method.

## Next steps

You can easily extend the above to collect multiple different types of
measurements for many different projects, and add all of those to graphs in your
`insights.html`. It's just more of the same.

Also, as I'm writing this blog post, I'm thinking that it should probably also be
possible to setup Grafana in such a way that you can view the graphs in your
dashboard. This of course requires you to run Grafana somewhere (so no longer
without third-party tools), but that might be a nice next step.


[github-pages]: https://pages.github.com
[bitbucket-pages]: https://support.atlassian.com/bitbucket-cloud/docs/publishing-a-website-on-bitbucket-cloud/
[gitlab-pages]: https://docs.gitlab.com/ee/user/project/pages/
[clojure]: https://clojure.org
[leiningen]: https://leiningen.org
[cloverage]: https://github.com/cloverage/cloverage
[circleci-add-keys]: https://circleci.com/docs/2.0/add-ssh-key/
[^or-branch]: Some platforms also support using a separate branch of your normal
source code repository instead of a separate repository.
