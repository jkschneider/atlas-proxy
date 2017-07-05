# Atlas Proxy

## Purpose

[Netflix Atlas](https://github.com/Netflix/atlas) is a backend for managing dimensional time series data, especially for application monitoring.
This project proxies or embeds the Atlas service to provide additional functionality, including:

1. An imperative Groovy-based query language that translates to the Atlas stack language.
2. A simple UI for entering queries and displaying resultant graphs.

## Building from source

1. `cd service`
2. `./gradlew build devSnapshot` (or substitute `devSnapshot` for `final` to build a release)

Your distribution will be in `/service/build/libs/atlas-proxy-VERSION.jar`
