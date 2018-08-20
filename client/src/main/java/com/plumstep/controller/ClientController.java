package com.plumstep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

@RestController
public class ClientController {
    @Autowired
    private RestTemplate restTemplate;
    private String serverUrl = "https://localhost:8111/server/";

    @RequestMapping(value = "client", method = RequestMethod.GET)
    ResponseEntity<?> getMessage() {
	    return restTemplate.getForEntity(serverUrl, String.class);
    }

    /*
     * Return the authenticated username and roles.
     */
    @GetMapping("/test")
    public String whoami() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        // The usom-correlationid is being set as a header by the interceptor so we don't care what this uuid is.
        httpHeaders.set("UUID", UUID.randomUUID().toString());
        httpHeaders.set("api_key", "7ab4c12e-c064-4a49-9538-e75c59175e66");

        try {
            return restTemplate.exchange(
//                    "https://localhost:8111/server/",
                    "https://thdapi.homedepot.com/cts/api/v1/sku/productCode/search?sku=218340",
                    HttpMethod.GET,
                    new HttpEntity(httpHeaders),
                    String.class
            ).getBody();
        } catch (HttpServerErrorException ex) {
            throw ex;
        }
    }
}
