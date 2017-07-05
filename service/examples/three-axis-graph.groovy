Timer t = timer('playback.startLatency')
Counter c = counter('sps')

// draw 3 different lines
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
