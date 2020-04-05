package bitronix.tm.integration.cdi.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author aschoerk
 */
@Entity
@Table(name = "test_entity_1")
public class TestEntity1 {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    private String stringAttribute;

    private int intAttribute;

    public TestEntity1() {

    }

    public Long getId() {
        return id;
    }

    public void setId(long idP) {
        this.id = idP;
    }

    public String getStringAttribute() {
        return stringAttribute;
    }

    public void setStringAttribute(String stringAttributeP) {
        this.stringAttribute = stringAttributeP;
    }

    public int getIntAttribute() {
        return intAttribute;
    }

    public void setIntAttribute(int intAttributeP) {
        this.intAttribute = intAttributeP;
    }
}
