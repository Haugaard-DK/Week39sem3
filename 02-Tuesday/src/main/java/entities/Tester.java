package entities;

import java.util.List;
import javax.persistence.Persistence;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
/**
 *
 * @author Mathias
 */
public class Tester {
    
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pu");
        EntityManager em = emf.createEntityManager();
        
        Person p1 = new Person("Nikolaj", 2000);
        Person p2 = new Person("Nicklas", 2020);
        
        Address a1 = new Address("EnGade", 1234, "EnBy");
        Address a2 = new Address("ToGade", 4321, "ToBy");
        
        p1.setAddress(a1);
        p2.setAddress(a2);
        
        Fee f1 = new Fee(100);
        Fee f2 = new Fee(500);
        Fee f3 = new Fee(750);
        
        p1.addFee(f2);
        p1.addFee(f3);
        p2.addFee(f1);
        
        SwimStyle s1 = new SwimStyle("Crawl");
        SwimStyle s2 = new SwimStyle("ButterFly");
        SwimStyle s3 = new SwimStyle("Breast Stroke");
        
        p1.addSwimStyle(s1);
        p2.addSwimStyle(s2);
        p1.addSwimStyle(s3);
        
        em.getTransaction().begin();
        em.persist(p1);
        em.persist(p2);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        p1.removeSwimStyle(s3);
        em.getTransaction().commit();
        
        System.out.println("p1: " + p1.getP_id()+", " + p1.getName());
        System.out.println("p2: " + p2.getP_id()+", " + p2.getName());
        
        System.out.println("Nikolajs gade: " + p1.getAddress().getStreet());
        
        System.out.println("Lad os se om to-vejs virker: " + a1.getPerson().getName());
        
        System.out.println("Hvem har betalt f2? Det har: " + f2.getPerson().getName());
        
        System.out.println("Hvad er der blevet betalt i alt?");
        
        TypedQuery<Fee> q1 = em.createQuery("SELECT f FROM Fee f", Fee.class);
        List<Fee> fees = q1.getResultList();
        
        for (Fee f : fees){
            System.out.println(f.getPerson().getName() + ": " + f.getAmount() + " kr. Den: " + f.getPayDate() + " Adr: " + f.getPerson().getAddress().getCity());
        }
    }

}
