# What do I do when I discover high latency?

Your P95 is suddenly surpassing your 500ms threshold. What do you do?

We need to investigate what's causing the spikes.

1. Are they intermittent or happening consistently?
1. Do they correspond with high CPU, memory, or network utilization?
1. Do they correspond with a recent deployment of new code or infrastructure?

## Separate errors and successes

First, distinguish between successful and non-successful (error) requests. Your
errors may be skewing the average, or vice-versa.

Figuring if one or the other is slow will hint to which parts of your code or
infrastructure you need to dive into next.

## Rely on the other of the Four Golden Signals

Latency is one of them.

The other four are Traffic, Errors, and Saturation.

Investigating these metrics may make it clear what's causing the spike in
latency.

## Engage the system experts

SREs are often working with black boxes in terms of what's running on the cloud
infrastructure they manage.
