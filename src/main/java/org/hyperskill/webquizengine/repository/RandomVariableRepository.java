package org.hyperskill.webquizengine.repository;

import org.hyperskill.webquizengine.model.RandomVariable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RandomVariableRepository extends PagingAndSortingRepository<RandomVariable, Long> {
}