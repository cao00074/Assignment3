
/********************************************************************egg***m******a**************n************
 * File: Employee.java
 * Course materials (19W) CST 8277
 * @author Mike Norman
 * (Modified) @date 2019 03
 *
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Original @authors dclarke, mbraeuer
 */
package com.algonquincollege.cst8277.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * The Employee class demonstrates several JPA features:
 * <ul>
 * <li>Generated Id
 * <li>Version locking
 * <li>OneToOne relationship
 * <li>OneToMany relationship
 * <li>ManyToMany relationship
 * </ul>
 */
@Entity
@NamedQueries({
    @NamedQuery(
            name = "findAllEmployeesWithSIm",
            query="select e from Employee e where e.lastName like :lastNmae")
})
@Table(name = "employee")
public class Employee extends ModelBase implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    // TODO - additional persistent field
    protected String firstName;
    protected String lastName;
    protected double salary;   
    protected Address address;
    protected List<Phone> phones = new ArrayList<>();
    protected List<Project> projects = new ArrayList<>();
    
   
 
    // JPA requires each @Entity class have a default constructor
    public Employee() {
        super();
    }
    
    @OneToOne()
    @JoinColumn(name = "ADDR_ID",referencedColumnName = "id")
    public Address getAddress() {
        return address;
    }    
      
    public void setAddress(Address address) {
        this.address = address;
    }
    
    @OneToMany(mappedBy = "employee")
    public List<Phone> getPhones(){
        return phones;
    }
    
    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }
    
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "EMP_PROJ",joinColumns = {@JoinColumn(name = "EMP_ID")},
     inverseJoinColumns = {@JoinColumn(name = "PROJ_ID")} )
    public List<Project> getProjects(){
        return projects;
    }
    
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
    
    @Column(name = "FIRSTNAME")
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Column(name = "LASTNAME")
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Column(name = "SALARY")
    public double getSalary() {
        return salary;
    }
    public void setSalary(double salary) {
        this.salary = salary;
    }
    
    
    
    // Strictly speaking, JPA does not require hashcode() and equals(),
    // but it is a good idea to have one that tests using the PK (@Id) field



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Employee)) {
            return false;
        }
        Employee other = (Employee)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

}