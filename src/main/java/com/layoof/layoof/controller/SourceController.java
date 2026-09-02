package com.layoof.layoof.controller;

import com.layoof.layoof.service.SourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/source")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @GetMapping("/count")
    public long count() {
        return sourceService.count();
    }
}
