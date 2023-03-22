package com.griddynamics.typeaheads.repository;

import com.griddynamics.typeaheads.model.TypeaheadServiceRequest;
import com.griddynamics.typeaheads.model.TypeaheadServiceResponse;

public interface TypeaheadRepository {
    TypeaheadServiceResponse getAllTypeaheads(TypeaheadServiceRequest request);
    TypeaheadServiceResponse getTypeaheadsByQuery(TypeaheadServiceRequest request);

    void recreateIndex();
}
