package application.repository.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class Weight implements Comparable<Weight>{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    private int level;

    private int number;

    private int backNumber;

    private double value;

    private String name;

    public Weight(final int level, final int number, final double value, int backNumber, String name){
        this.level = level;
        this.number = number;
        this.value = value;
        this.backNumber = backNumber;
        this.name = name;
    }

    @Override
    public int compareTo(Weight o) {
        return Integer.compare(id, o.id);
    }

    public Weight clone(String name){
        return new Weight(level, number, value, backNumber, name);
    }
}
