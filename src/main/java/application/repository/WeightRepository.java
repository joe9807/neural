package application.repository;

import application.repository.entity.Weight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeightRepository extends CrudRepository<Weight, Integer> {
    List<Weight> findAllByLevel(int level);

}