package com.griddynamics.es.graduation.project.repository;

import com.griddynamics.es.graduation.project.model.TypeaheadServiceRequest;
import com.griddynamics.es.graduation.project.model.TypeaheadServiceResponse;

public interface TypeaheadRepository {
    TypeaheadServiceResponse getAllTypeaheads(TypeaheadServiceRequest request);
    TypeaheadServiceResponse getTypeaheadsByQuery(TypeaheadServiceRequest request);

    void recreateIndex();
}
