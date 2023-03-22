package com.griddynamics.typeaheads.rest;

import com.griddynamics.typeaheads.model.TypeaheadServiceRequest;
import com.griddynamics.typeaheads.model.TypeaheadServiceResponse;
import com.griddynamics.typeaheads.service.TypeaheadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/v1/typeahead")
public class TypeaheadController {

    @Autowired
    private TypeaheadService typeaheadService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public TypeaheadServiceResponse getSearchServiceResponse(@RequestBody TypeaheadServiceRequest request) {
        return typeaheadService.getServiceResponse(request);
    }
}
