package org.firebirdsql.hibernate;


public class Person {
    
    private Integer id;
    
    private String name;
    
    private String surname;
    
    /**
     * Creates a new instance of Person
     */
    public Person() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
    
}
