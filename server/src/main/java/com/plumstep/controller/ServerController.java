package com.plumstep.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("server")
public class ServerController {
    @ApiOperation(value = "Return text message to show off successful call")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    ResponseEntity<?> getMessage() {
	return ResponseEntity.ok("Server successfully called!");
    }
}
