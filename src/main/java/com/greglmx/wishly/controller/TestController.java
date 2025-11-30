package com.greglmx.wishly.controller;

import com.greglmx.wishly.dto.TestResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public TestResponse greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name) {
        return new TestResponse("Hello " + name + "!");
    }
}
