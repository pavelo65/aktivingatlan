package com.aktivingatlan.repository;

import com.aktivingatlan.domain.Statement;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Statement entity.
 */
public interface StatementRepository extends JpaRepository<Statement,Long> {

    @Query("select distinct statement from Statement statement left join fetch statement.clients left join fetch statement.propertys")
    List<Statement> findAllWithEagerRelationships();

    @Query("select statement from Statement statement left join fetch statement.clients left join fetch statement.propertys where statement.id =:id")
    Statement findOneWithEagerRelationships(@Param("id") Long id);

}
