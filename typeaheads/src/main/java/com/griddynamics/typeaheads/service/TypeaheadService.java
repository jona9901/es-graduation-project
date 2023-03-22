package com.griddynamics.typeaheads.service;

import com.griddynamics.typeaheads.model.TypeaheadServiceRequest;
import com.griddynamics.typeaheads.model.TypeaheadServiceResponse;

public interface TypeaheadService {
    TypeaheadServiceResponse getServiceResponse(TypeaheadServiceRequest request);

    void recreateIndex();
}
