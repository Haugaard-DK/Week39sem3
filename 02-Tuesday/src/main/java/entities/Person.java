package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 *
 * @author Mathias
 */
@Entity
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long p_id;
    private String name;
    private int year;
    
    @OneToOne(cascade = CascadeType.PERSIST)
    private Address address;
    
    @OneToMany(mappedBy = "person", cascade = CascadeType.PERSIST)
    private List<Fee> fees;
    
    @ManyToMany(mappedBy = "persons", cascade = CascadeType.PERSIST)
    List<SwimStyle> styles;
    
    public Person(){
    }

    public Person(String name, int year) {
        this.name = name;
        this.year = year;
        fees = new ArrayList<>();
        styles = new ArrayList<>();
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void addFee(Fee fee) {
        fees.add(fee);
        if(fee != null){
            fee.setPerson(this);
        }
    }
    
    public void addSwimStyle(SwimStyle swimStyle) {
        styles.add(swimStyle);
        if(swimStyle != null){
            this.styles.add(swimStyle);
            swimStyle.getPersons().add(this);
        }
    }
    
    public void removeSwimStyle(SwimStyle style){
        if(style != null){
            styles.remove(style);
            style.getPersons().remove(this);
        }
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
        if(address != null){
            address.setPerson(this);
        }
    }
    
    public Long getP_id() {
        return p_id;
    }

    public void setP_id(Long p_id) {
        this.p_id = p_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    
    


}
