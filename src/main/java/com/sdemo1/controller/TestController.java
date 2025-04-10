package com.sdemo1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class TestController {


    @GetMapping("/")
    public String index(){

        log.trace("TRACE!!");
        log.debug("DEBUG!!");
        log.info("INFO!!");
        log.warn("WARN!!");
        log.error("ERROR!!");

        return "index";
    }

    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("data", "test~~!!");
        return "test";
    }

    // http://localhost:8080/test-mvc?name=111
    @GetMapping("/test-mvc")
    public String testMvc(@RequestParam("name") String name, Model model) {
        model.addAttribute("name", name);
        return "test-template";
    }

    @GetMapping("/test-string")
    @ResponseBody
    public String testString(@RequestParam("name") String name) {
        return "your name is " + name;
    }

    @GetMapping("test-api")
    @ResponseBody
    public test testApi(@RequestParam("name") String name) {
        test test = new test();
        test.setName(name);
        return test;
    }

    static class test {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
