package io.pivotal.atlas.groovy.script

class InvalidTimeSeriesException(val timeSeries: String, msg: String): Exception(msg)
