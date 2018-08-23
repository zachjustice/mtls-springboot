package ca.oakey.samples.web.clientauth;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@RestController
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private String url;
    private RestTemplate restTemplate;

    @Autowired
    public TestController(
            @Value("${url}") String url,
            RestTemplate restTemplate
    ){
        this.url = url;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/test")
    public String test() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("UUID", UUID.randomUUID().toString());
        httpHeaders.set("api_key", "7ab4c12e-c064-4a49-9538-e75c59175e66");

        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity(httpHeaders),
                    String.class
            ).getBody();
        } catch (HttpServerErrorException ex) {
            logger.error(ex.getMessage());
            throw ex;
        }
    }

}