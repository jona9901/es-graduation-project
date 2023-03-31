package com.griddynamics.indexer.controllers;

import com.griddynamics.indexer.services.ProductIndexerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ProductIndexerController {
    private final ProductIndexerService productIndexerService;
    @GetMapping("/recreate")
    public void recreateIndex() {
        productIndexerService.recreateIndex();
    }
}
