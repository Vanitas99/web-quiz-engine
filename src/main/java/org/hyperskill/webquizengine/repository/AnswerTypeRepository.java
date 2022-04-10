package org.hyperskill.webquizengine.repository;

import org.hyperskill.webquizengine.model.AnswerTestType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerTypeRepository extends CrudRepository<AnswerTestType, Long> {
}
