package com.fitnessclub.membership_portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({ "/", "/index" })
    public String home() {
        return "index";
    }
}
