package application.neural;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NeuronBackLevel extends NeuronLevel{
    @Autowired
    private WeightRepository weightRepository;

    protected List<Neuron> getNeurons(int level, List<Double> input, List<Double> delta) {
        List<Neuron> neurons = new ArrayList<>();
        List<Weight> allWeights = weightRepository.findAllByLevel(level);

        final AtomicInteger number = new AtomicInteger();
        IntStream.range(0, input.size()).forEach(pos->{
            neurons.add(new NeuronBack(level-1, number.get(), getBackWeights(allWeights, pos).stream().sorted().map(Weight::getValue).collect(Collectors.toList()), input, delta));
            number.getAndIncrement();
        });

        return neurons;
    }

    private List<Weight> getBackWeights(List<Weight> allWeights, int pos){
        List<Weight> result = new ArrayList<>();
        AtomicInteger number = new AtomicInteger(0);
        AtomicInteger atomicPos = new AtomicInteger(pos);
        allWeights.stream().sorted().forEach(weight->{
            if (number.get() == weight.getNumber()) {
                if (atomicPos.get() == 0) {
                    result.add(weight);
                    atomicPos.set(pos);
                    number.getAndIncrement();
                } else {
                    atomicPos.getAndDecrement();
                }
            }
        });

        return result;
    }
}
