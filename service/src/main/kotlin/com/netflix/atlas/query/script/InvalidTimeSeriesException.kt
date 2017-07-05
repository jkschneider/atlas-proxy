package com.netflix.atlas.query.script

class InvalidTimeSeriesException(val timeSeries: String, msg: String): Exception(msg)
