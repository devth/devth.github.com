---
layout: article
title: "Fast app generation and deployment guide for Startup Weekend"
categories: infra
comments: true
excerpt: The aim of this guide is to demonstrate the fastest way to create a client-side web app and get it deployed.
published: true
toc: true
image:
  feature: fast_app_feature.jpg
  teaser: fast_app_teaser.jpg
  caption: Leaving Montana
---

The aim of this guide is to demonstrate the fastest way to create a client-side
web app and get it deployed. I'm writing it for myself in preparation for an
upcoming [Startup Weekend
event](http://communities.techstars.com/usa/colorado-springs/startup-weekend/13311).
We'll keep it CLI-heavy and automation-friendly as opposed to point-and-clicking
our way through web dashboards.

## Tech

- [GitLab](https://gitlab.com) - GitLab's unlimited private repos, amazing CI
  and free public pages make it an excellent choice for deploying static sites.
- React - [`create-react-app`](https://github.com/facebook/create-react-app)
  is the quickest way to start hacking on React without messing around with
  Babel and Webpack configs.
- ES6 - we'll use plain ES6 instead of something fancier like TypeScript,
  ReasonML, or Elm because the focus is speed and simplicity.

## Caveats

- We'll eschew typical production concerns like performance, security,
  configuration, and tests since we're aiming to build an MVP in a weekend.
- Even though a web app will likely need some CRUD we won't spend the time and
  effort to use an actual database; instead the prototype will use client-side
  state to simulate how it will work.

## Prerequisites

This guide assumes [Homebrew](https://brew.sh/) but the following packages can
be installed by many other means.

```bash
brew install yarn
brew install jq
brew install ruby
```

## GitLab

Let's create a GitLab group so we can collaborate with our teammates.

```bash
gem install gitlab
export GITLAB_API_ENDPOINT=https://gitlab.com/api/v4

# visit https://gitlab.com/profile/personal_access_tokens and generate a token
echo "Enter your GitLab token"
 read -s gitlab_token
export GITLAB_API_PRIVATE_TOKEN=$gitlab_token

gitlab user # make sure we're authenticated
user_id=`gitlab user --json | jq '.result.id'`
echo $gitlab_user_id # this should be an integer

gitlab help create_group
# pick your own unique group name and group path
# these are global on GitLab
group_json=`gitlab create_group startup-weekend-2019 startup-weekend-2019 --json`
echo $group_json | jq
group_id=`echo $group_json | jq '.result.id'`
echo $group_id # should be an integer

# add your teammates (you'll need their GitLab user IDs)
gitlab help add_group_member
# we'll give everyone owner permissions for this exercise
# see https://docs.gitlab.com/ee/api/access_requests.html for docs
access_level=50
gitlab add_group_member $group_id $user_id $access_level
# the above will fail because we're already a member
# instead perform this for gitlab users on your team that you want to add
# you can do this via the web dashboard if you like
```

Now that our group is setup and members added, let's create the repo under it.

```bash
gitlab help create_project
project_json=`gitlab create_project app "{namespace_id: '$group_id'}" --json`
# confirm it succeeded:
echo $project_json
```

## Node.js React App

Let's generate a fresh React app:

```bash
yarn global add create-react-app
create-react-app startup-weekend
cd startup-weekend
yarn install
```

Now configure the git repo we setup in the previous step and push to it:

```bash
git remote add origin `echo $project_json | jq -r '.result.ssh_url_to_repo'`
git push -u origin master
```

Congratulations, your app source is now safe in GitLab. The next step is to
deploy!

## Deployment on GitLab Pages

First we need to specify the `homepage` property in `package.json` so CRA
computes the correct asset paths when it builds assets.

```bash
group_path=`echo $group_json | jq -r '.result.full_path'`
project_path=`echo $project_json | jq -r '.result.path'`
gitlab_pages_url=`echo https://$group_path.gitlab.io/$project_path`

tmp=$(mktemp)
cat package.json \
  | jq --arg homepage $gitlab_pages_url '. + {homepage: $homepage}' > $tmp \
  && mv $tmp package.json

git commit -am 'Add homepage to package.json'
```

Now we can setup CI. Run this to add a `.gitlab-ci.yml` to your repo specifying
how to build in GitLab CI and deploy to GitLab Pages:

```bash
cat << 'EOF' > .gitlab-ci.yml
cache:
  untracked: true
  key: "$CI_BUILD_REF_NAME"
  paths:
    - node_modules/
    - .yarn-cache

image: node

stages:
  - deps
  - test
  - publish

deps:
  stage: deps
  script:
    - yarn config set cache-folder .yarn-cache
    - yarn install --pure-lockfile

pages: # must be named pages to publish to GitLab pages!
  stage: publish
  script:
    - yarn build
    # replace public contents with build - GitLab requires a `public` dir
    - rm -rf public/*
    - mv build/* public
    - ls -al public/
  artifacts:
    paths:
    - public
EOF
```

With this in place, GitLab CI will build and deploy our app on every `git push`.
Let's try it out:

```bash
git add .gitlab-ci.yml && git commit -m 'Setup GitLab CI deployments'
git push
```

Open up the pipelines dashboard and sit back while your app is built and
deployed:

```bash
open `echo $project_json | jq -r '.result.web_url'`/pipelines
```

Once it's finished, view your app:

```bash
open $gitlab_pages_url
```

At this point you can start developing your app and have it auto deployed any
time someone pushes to `master`.

## Closing thoughts

One of the [Startup Weekend
resources](https://startupweekend.org/attendees/resources) is $3k worth of
credits on Google Cloud Platform. While GCP is an excellent option for real
apps, I opted to keep things as minimal as possible, and it ended up being free.

We could very easily extend this further using the [Firebase Realtime
Database](https://firebase.google.com/docs/database/) and [Firebase
Authentication](https://firebase.google.com/docs/auth/) which gets you Google /
Facebook / Twitter / GitHub login and a schemaless data store with very little
work. These options change nothing from a deployment standpoint as Firebase
usage remains purely client side. And since Firebase is a part of the GCP
offering you can use those credits.
