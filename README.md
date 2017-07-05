# Atlas Proxy

## Purpose

[Netflix Atlas](https://github.com/Netflix/atlas) is a backend for managing dimensional time series data, especially for application monitoring.
This project proxies or embeds the Atlas service to provide additional functionality, including:

1. An imperative Groovy-based query language that translates to the Atlas stack language.
2. A simple UI for entering queries and displaying resultant graphs.

## Getting started

Download the JAR distribution from the [Releases](https://github.com/jkschneider/atlas-proxy/releases) page. Run with `java -jar atlas-proxy-VERSION.jar`.

## Atlas Groovy

Below is an example query demonstrating a variety of features of Groovy-based query language:

```groovy
Timer t = select.timer('playback.startLatency')
Counter c = select.counter('sps')

// draw 3 different lines, originating from two different metrics
graph
        .line(t.latency().lineWidth(2).axis(0))
        .line(t.throughput().axis(1))
        .line(c.axis(2))

// set display options
graph
        .title('Playback Starts and SPS')
        .timeZone('US/Central')
        .axisLabel(0, 'request latency')
        .axisLabel(1, 'throughput')
        .axisLabel(2, 'sps')
```

View other sample queries in the [examples](https://github.com/jkschneider/atlas-proxy/tree/master/examples) folder.

## Building from source

1. `cd service`
2. `./gradlew build devSnapshot` (or substitute `devSnapshot` for `final` to build a release)

Your distribution will be in `/service/build/libs/atlas-proxy-VERSION.jar`
