---
layout: article
title: Yetibot on Docker in 𝓧 minutes or less
categories: yetibot
comments: true
excerpt: Yetibot is now on Docker!
image:
  feature: on_docker_wide.jpg
  teaser: on_docker_teaser.jpg
  caption: Yetibot Source Code, January 2016
---

Yetibot is now [on Docker](https://hub.docker.com/r/devth/yetibot/tags/)! This
is the fastest way to get up and running. To demonstrate, let's run it with
Docker using the most minimal configuration possible: an in-memory
(non-durable) Datomic configuration and a single IRC adapter config. (I'm
assuming your local Docker is all [setup and
configured](https://www.docker.com/docker-toolbox).)

```bash
mkdir -p ~/tmp/config

cat << EOF > ~/tmp/config/config.edn
{:yetibot
 {:db {:datomic-url "datomic:mem://yetibot"}
  :adapters
  [{:name "freenode-irc",
    :type :irc,
    :host "chat.freenode.net",
    :port "6665",
    :username "yetibot-docker"
    :rooms #{"#yetibot"}}]}}
EOF

docker run --name yetibot \
  -d -p 3000:3000 \
  -v ~/tmp/config:/usr/src/app/config \
  devth/yetibot

docker logs -f yetibot
```

**N.B.** I chose `~/tmp/config` because:

> If you are using Docker Machine on Mac or Windows, your Docker daemon has only
> limited access to your OS X or Windows filesystem. Docker Machine tries to
> auto-share your /Users (OS X) or C:\Users (Windows) directory.
> — [Mount a host directory as a data volume](https://docs.docker.com/engine/userguide/dockervolumes/#mount-a-host-directory-as-a-data-volume)

If you're not using Docker Machine feel free to put it wherever you like.

Hop on Freenode and join the #yetibot channel. Once `yetibot-docker` joins, try
it out!

```
!list yetibot on docker | xargs echo ⚡️⚡️⚡️ %s ⚡️⚡️⚡️
```


<img src="/images/yetibot-on-docker-irc.png" />

When you're done, clean up with:

```bash
docker rm -f yetibot
```

To get the most out of Yetibot you'll want to configure some of the optional
services. See the full [config
sample](https://github.com/devth/yetibot/blob/master/config/config-sample.edn)
and check out [yetibot.com](http://yetibot.com) for more info on the cool things
you can do with Yetibot!

<small><sub>where 𝓧 ≈ somewhere between 1 and 3.5 minutes</sub></small>
