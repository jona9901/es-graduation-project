package com.griddynamics.pss.controllers;

import com.griddynamics.indexer.services.ProductIndexerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ProductController {
    private final ProductIndexerService productIndexerService;

    @GetMapping("/product")
    public void product(){
        productIndexerService.recreateIndex();
    }
}
