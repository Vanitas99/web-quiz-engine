package org.hyperskill.webquizengine.repository;

import org.hyperskill.webquizengine.model.MaximaQuestion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MaximaQuestionRepository extends PagingAndSortingRepository<MaximaQuestion, Long> {

    Slice<MaximaQuestion> findQuestionsByCategory(String category, Pageable pageable);
}
