package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    }

    public void deleteAll(){
        weightRepository.deleteAll();
        weights = new HashMap<>();
    }

    public Iterable<Weight> saveAll(List<Weight> all){
        return weightRepository.saveAll(all);
    }

    public void saveWithName(String name){
        weights.values().forEach(level->{
            saveAll(level.stream().map(weight-> weight.clone(name)).collect(Collectors.toList()));
        });
    }
}
