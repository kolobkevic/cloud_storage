package ru.kolobkevic.cloud_storage.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("")
    public String mainPage(){
        return "index";
    }
}
