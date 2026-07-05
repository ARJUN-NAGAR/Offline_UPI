package com.demo.offline_upi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to render the main simulation dashboard page.
 */
@Controller
public class DashboardController {

    @GetMapping("/")
    public String index() {
        return "dashboard"; // Renders templates/dashboard.html
    }
}
