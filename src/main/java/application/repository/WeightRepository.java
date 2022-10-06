package application.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import application.repository.entity.Weight;

import java.util.List;

@Repository
public interface WeightRepository extends CrudRepository<Weight, Integer> {
    @Query("from Weight w where w.level=:level order by id")
    List<Weight> findAllByLevel(@Param("level") int level);
}