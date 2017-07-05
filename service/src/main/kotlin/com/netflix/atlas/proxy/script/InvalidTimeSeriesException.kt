package com.netflix.atlas.proxy.script

class InvalidTimeSeriesException(val timeSeries: String, msg: String): Exception(msg)
