package application.repository.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class Weight implements Comparable<Weight>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private double value;

    private int level;

    private int number;

    @Override
    public int compareTo(Weight o) {
        return Integer.compare(o.id, id);
    }
}
