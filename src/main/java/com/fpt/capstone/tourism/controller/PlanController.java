package com.fpt.capstone.tourism.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/plan")
public class PlanController {


    @GetMapping("/location")
    public String getLocation() {
        return "Get location";
    }


}
