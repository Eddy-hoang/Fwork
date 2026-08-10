package com.intern.fwork.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String common() {
        return "Hello Everyone";
    }

}
