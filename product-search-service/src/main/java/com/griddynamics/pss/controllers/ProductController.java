package com.griddynamics.pss.controllers;

import com.griddynamics.pss.models.ProductRequest;
import com.griddynamics.pss.models.ProductResponse;
import com.griddynamics.pss.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/product")
    public @ResponseBody ResponseEntity<ProductResponse> product(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.getServiceResponse(request), HttpStatus.OK);
    }

    @PostMapping("/recreate/index")
    public void recreateIndex() {
        productService.recreateIndex();
    }
}
