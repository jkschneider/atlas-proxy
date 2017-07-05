package com.netflix.atlas.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AtlasProxyApplicationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void query() {
        String response = restTemplate.postForObject("/api/graph",
                "graph.line(timer('playback.startLatency').latency())",
                String.class);


        System.out.println(response);
    }
}
