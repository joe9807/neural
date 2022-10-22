package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NeuralRepository {
    @Autowired
    private WeightRepository weightRepository;

    private Map<Integer, List<Weight>> weights = new HashMap<>();

    List<Weight> findAllByLevel(int level){
        if (weights.get(level) == null){
            weights.put(level, weightRepository.findAllByLevel(level));
        }

        return weights.get(level);
    }

    public void saveAll() {
        weights.values().forEach(value-> weightRepository.saveAll(value));
        weights = new HashMap<>();
    }

    public void deleteAll(){
        weightRepository.deleteAll();
    }

    public Iterable<Weight> saveAll(List<Weight> all){
        return weightRepository.saveAll(all);
    }
}
