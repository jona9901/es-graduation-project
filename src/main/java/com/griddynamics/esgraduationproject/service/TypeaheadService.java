package com.griddynamics.es.graduation.project.service;

import com.griddynamics.es.graduation.project.model.TypeaheadServiceRequest;
import com.griddynamics.es.graduation.project.model.TypeaheadServiceResponse;

public interface TypeaheadService {
    TypeaheadServiceResponse getServiceResponse(TypeaheadServiceRequest request);

    void recreateIndex();
}
