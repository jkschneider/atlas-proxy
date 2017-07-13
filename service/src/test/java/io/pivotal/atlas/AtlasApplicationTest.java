package io.pivotal.atlas;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"atlas.embedded=false"})
public class AtlasApplicationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void query() {
        String response = restTemplate.postForObject("/api/graph",
                "graph.line(select.timer('playback.startLatency').latency())",
                String.class);

        assertTrue(response.contains("name,playback.startLatency,:eq"));
    }
}
