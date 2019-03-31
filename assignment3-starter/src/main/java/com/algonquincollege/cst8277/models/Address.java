/********************************************************************egg***m******a**************n************
 * File: Address.java
 * Course materials (19W) CST 8277
 * @author Mike Norman
 * (Modified by Chenxiao Cui and Lei Cao) @date 2019 03
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
 *
 */
package com.algonquincollege.cst8277.models;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Simple Address class - it uses a generated Id.
 */
@Entity
@NamedQueries({
    @NamedQuery(
            name = "findEmployeesWithPostal",
            query = "select a from Address a where a.postal like :postal"),
    @NamedQuery(
            name = "countAddress",
            query = "select count(a) from Address a"),
    @NamedQuery(
            name = "findEmployeesByStreet",
            query = "select a from Address a where a.street like :street"),
    @NamedQuery(
            name = "findEmployeesInNepean",
            query = "select a from Address a where a.city like :city") 
})
@Table(name = "address")
public class Address extends ModelBase implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    // Additional persistent field
    protected String city;
    protected String country;
    protected String postal;
    protected String state;
    protected String street;
    protected Employee employee;
        
    @OneToOne(mappedBy = "address", cascade={CascadeType.ALL})
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    // JPA requires each @Entity class have a default constructor
    public Address() {
        super();
    }

    @Column(name = "CITY")
    public String getCity() {
        return city;
    }


    public void setCity(String city) {
        this.city = city;
    }

    @Column(name = "COUNTRY")
    public String getCountry() {
        return country;
    }


    public void setCountry(String country) {
        this.country = country;
    }

    @Column(name = "POSTAL")
    public String getPostal() {
        return postal;
    }

    
    public void setPostal(String postal) {
        this.postal = postal;
    }

    @Column(name = "STATE")
    public String getState() {
        return state;
    }


    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "STREET")
    public String getStreet() {
        return street;
    }


    public void setStreet(String street) {
        this.street = street;
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
        if (!(obj instanceof Address)) {
            return false;
        }
        Address other = (Address)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

}