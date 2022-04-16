package org.hyperskill.webquizengine.repository;

import org.hyperskill.webquizengine.model.RandomizedQuestion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RandomizedQuestionRepository extends CrudRepository<RandomizedQuestion, Long> {
}
