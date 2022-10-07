package application.neurolevels;

import application.repository.WeightRepository;
import application.repository.entity.Weight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class NeuronLevel {
    private static final int CORE = 20;
    private final ThreadPoolExecutor executor;

    @Autowired
    private WeightRepository repository;

    public NeuronLevel(){
        executor = new ThreadPoolExecutor(CORE, CORE, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(CORE), getThreadFactory());
    }

    private ThreadFactory getThreadFactory(){
        return new ThreadFactory(){
            private final AtomicInteger count = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.valueOf(count.getAndIncrement()));
            }
        };
    }

    private List<Neuron> getNeurons(int level, List<Double> input) {
        List<Neuron> neurons = new ArrayList<>();
        List<Weight> all = repository.findAllByLevel(level);

        final AtomicInteger number = new AtomicInteger();
        while (true) {
            List<Double> weights = all.stream().filter(weight-> weight.getNumber() == number.get()).sorted().map(Weight::getValue).collect(Collectors.toList());
            if (weights.size() == 0) break;
            neurons.add(new Neuron(number.get(), weights, getInput(level, input, number.get())));
            number.getAndIncrement();
        }
        return neurons;
    }

    private List<Double> getInput(int level, List<Double> input, int number){
        if (input == null) return Collections.singletonList(Math.random());
        if (level == 0) return Collections.singletonList(input.get(number));
        return input;
    }

    public List<Double> calculate(int level, List<Double> input){
        final List<Neuron> neurons = getNeurons(level, input);
        final List<Neuron> neuronsForRemove = new ArrayList<>();
        final List<Future<?>> futures = new ArrayList<>();

        do {
            for (Neuron neuron : neurons) {
                if (executor.getQueue().size() == CORE) continue;

                futures.add(executor.submit(new NeuronWorker(neuron)));
                neuronsForRemove.add(neuron);
            }

            neurons.removeAll(neuronsForRemove);
        } while (neurons.size() != 0 || executor.getActiveCount() != 0);


        return futures.stream().map(future->{
            try {
                return (Neuron)future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).sorted().map(Neuron::getSum).collect(Collectors.toList());
    }
}
