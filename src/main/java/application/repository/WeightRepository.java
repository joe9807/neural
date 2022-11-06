package application.repository;

import application.repository.entity.Weight;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WeightRepository extends CrudRepository<Weight, Integer> {
    List<Weight> findAllByLevel(int level);

    void deleteByName(String name);

    @Transactional
    @Modifying
    @Query(value = "delete from weight where name = ''", nativeQuery = true)
    void deleteCurrent();

    List<Weight> findAllByName(String name);

    @Query(value = "select distinct name from weight", nativeQuery = true)
    List<String> getAllNames();
}