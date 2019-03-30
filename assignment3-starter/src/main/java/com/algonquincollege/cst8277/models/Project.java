/********************************************************************egg***m******a**************n************
 * File: Project.java
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
 */
package com.algonquincollege.cst8277.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;



/**
 * The Project class demonstrates:
 * <ul>
 * <li>Generated Id
 * <li>Version locking
 * <li>ManyToMany mapping
 * </ul>
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "countProject",       
            query = "select count(p) from Project p"),
        @NamedQuery(name = "countEmployee",       
            query = "select count(e) from Employee e")
})
@Table(name = "project")
public class Project extends ModelBase implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    // TODO - persistent fields
    //protected int proId;
    protected String description;
    protected String name;
    protected List<Employee> employees = new ArrayList<>() ;
    //protected int proVersion;

    @ManyToMany(mappedBy = "projects")
    public List<Employee> getEmployees(){
        return employees;
    }
    
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
    
    // JPA requires each @Entity class have a default constructor
    public Project() {
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
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
        if (!(obj instanceof Project)) {
            return false;
        }
        Project other = (Project)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

}