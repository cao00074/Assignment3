/**********************************************************************egg*m******a******n********************
 * File: EmployeeTestSuite.java
 * Course materials (19W) CST 8277
 * @author (original) Mike Norman
 * Modified by Chenxiao Cui and Lei Cao
 * @date 2019 03
 * Description: Test cases for Employee, including CRUD and other scenarios.
 */
package com.algonquincollege.cst8277.models;

import static com.algonquincollege.cst8277.models.TestSuiteConstants.attachListAppender;
import static com.algonquincollege.cst8277.models.TestSuiteConstants.buildEntityManagerFactory;
import static com.algonquincollege.cst8277.models.TestSuiteConstants.detachListAppender;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeeTestSuite implements TestSuiteConstants {

    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LoggerFactory.getLogger(_thisClaz);
    private static final ch.qos.logback.classic.Logger eclipselinkSqlLogger =
        (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ECLIPSELINK_LOGGING_SQL);

    /**
     * Delcare test features - EntityManagerFactory and Server
     */
    public static EntityManagerFactory emf;
    public static Server server;
    
    /**
     * JPQL statements for checking actual SQL for every test case.
     */
    private static final String SELECT_EMPLOYEE_1 =
            "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (ID = ?)";
    private static final String CREATE_EMPLOYEE =
            "INSERT INTO employee (FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_EMPLOYEE =
            "UPDATE employee SET FIRSTNAME = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_EMPLOYEE_1 =
            "DELETE FROM employee WHERE (ID = ?)";
    private static final String DELETE_EMPLOYEE_2 =
            "DELETE FROM employee WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_EMP_PROJ_1 =
            "DELETE FROM EMP_PROJ WHERE EXISTS(SELECT ID FROM employee";
    private static final String DELETE_EMP_PROJ_2 =
            "DELETE FROM EMP_PROJ WHERE (EMP_ID = ?)";
    private static final String SELECT_HIGHEST_SALARY=
            "SELECT t0.ID, t0.FIRSTNAME, t0.LASTNAME, t0.SALARY, t0.VERSION, t0.ADDR_ID FROM employee t0 WHERE (t0.SALARY = (SELECT MAX(t1.SALARY) FROM employee t1))";
    private static final String SELECT_WITH_LAST_NAME=
            "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE LASTNAME LIKE ?";
    /**
     * Database environment set up, runs before class.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        try {
            logger.debug("oneTimeSetUp");
            // create in-process H2 server so we can 'see' into database
            // use "jdbc:h2:tcp://localhost:9092/mem:assignment3-testing" in Db Perspective
            // (connection in .dbeaver-data-sources.xml so should be immediately useable
            server = Server.createTcpServer().start();
            emf = buildEntityManagerFactory(_thisClaz.getSimpleName());
        }
        catch (Exception e) {
            logger.error("something went wrong building EntityManagerFactory", e);
        }
    }

    /**
     * Test Employee table is empty at the beginning.
     */
    @Test
    public void _01_test_no_Employees_at_start() {
        EntityManager em = emf.createEntityManager();

        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Employee emp1 = em.find(Employee.class,1);             //em.find Employee with PK 1
        detachListAppender(eclipselinkSqlLogger, listAppender);

        assertNull(emp1);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_EMPLOYEE_1));

        em.close();
    }

    /**
     * Test creating an employee (inserting a row in employee table).
     */
    @Test
    public void _02_test_create_employee() {
        Employee employee1 = new Employee();
        employee1.setId(1);
        employee1.setFirstName("FirstName");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(employee1);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> rootEmpQuery = cq.from(Employee.class);
        cq.select(rootEmpQuery);
        cq.where(cb.and(cb.equal(rootEmpQuery.get(Employee_.id),1)));
        TypedQuery<Employee> query =em.createQuery(cq);
        Employee employeeFromDB = query.getSingleResult();
        
        assertTrue(employeeFromDB.getFirstName()=="FirstName");
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(CREATE_EMPLOYEE));
        em.close();
    }
    
    /**
     * Test getting an employee from database with id
     */
    @Test
    public void _03_test_read_employee() {
        EntityManager em = emf.createEntityManager();
        Employee employee = (Employee) em.createQuery("select e from Employee e where e.id = 1")
                                         .getSingleResult();
        assertNotNull(employee);        
    }
    
    /**
     * Test updating an employee (updating a row in employee table).
     */
    @Test
    public void _04_test_update_employee() {
               
        EntityManager em = emf.createEntityManager();
        Employee employee1 = em.find(Employee.class, 1);
        employee1.setFirstName("UpdatedFirstName");
        em.getTransaction().begin();
        em.persist(employee1);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> rootEmpQuery = cq.from(Employee.class);
        cq.select(rootEmpQuery);
        cq.where(cb.and(cb.equal(rootEmpQuery.get(Employee_.id),1)));
        TypedQuery<Employee> query =em.createQuery(cq);
        Employee employeeFromDB = query.getSingleResult();
       // System.out.println("id: "+employeeFromDB.getId()+" FN: "+employeeFromDB.getFirstName());
        
        assertEquals("UpdatedFirstName", employeeFromDB.getFirstName());
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(UPDATE_EMPLOYEE));
        em.close();
    }
    
    /**
     * Test deleting an employee (deleting a row in employee table) using namedQuery.
     */
    @Test
    public void _05_test_delete_employee() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.createNamedQuery("deleteEmployee").executeUpdate();
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        Employee employee = em.find(Employee.class,1);List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(2, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(DELETE_EMP_PROJ_1));
        assertThat(loggingEvents.get(1).getMessage(),
                startsWith(DELETE_EMPLOYEE_1));
        assertNull(employee);        
    }
    
    /**
     * Test deleting an employee (deleting a row in employee table) using EntityManager.
     */
    @Test
    public void _06_test_delete_employee2() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Employee employeeNew = new Employee();
        employeeNew.setId(2);
        employeeNew.setLastName("Cui");
        em.persist(employeeNew);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().commit();
        Employee employee = em.find(Employee.class,2);
        assertNotNull(employee);
        em.getTransaction().begin();
        em.remove(employee);
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        employee = em.find(Employee.class,2);List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(4, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(CREATE_EMPLOYEE));
        assertThat(loggingEvents.get(1).getMessage(),
                startsWith("CALL IDENTITY()"));
        assertThat(loggingEvents.get(2).getMessage(),
                startsWith(DELETE_EMP_PROJ_2));
        assertThat(loggingEvents.get(3).getMessage(),
                startsWith(DELETE_EMPLOYEE_2));
        assertNull(employee);        
    }

    /**
     * Test getting an employee with highest salary.
     */
    @Test    
    public void _07_test_find_employee_with_highest_salary() {
        Employee employee1 = new Employee();
        employee1.setSalary(600);
        Employee employee2 = new Employee();
        employee1.setSalary(1000);        
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(employee1);
        em.persist(employee2);        
        em.getTransaction().commit();        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Employee highestSalary =(Employee)em.createNamedQuery("findHighestSalary").getSingleResult();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        assertTrue(highestSalary.getSalary()==1000); 
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_HIGHEST_SALARY));
        em.close();
    }
    
    /**
     * Test getting employees with same last name.
     */
    @Test    
    public void _08_test_find_employee_with_last_name() {
        Employee employee1 = new Employee();
        employee1.setLastName("Li");
        Employee employee2 = new Employee();
        employee2.setLastName("Li");        
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(employee1);
        em.persist(employee2);    
        em.getTransaction().commit();
        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        List<Employee> sameLastName =(List<Employee>)em.createNamedQuery("findEmployeesWithLastName").setParameter("lastName", "Li").getResultList();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        assertTrue(sameLastName.size()==2); 
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_WITH_LAST_NAME));
        em.close();
    }
    
    /**
     * Test getting the numbers of employees.
     */
    @Test    
    public void _09_test_count_employees() {          
        EntityManager em = emf.createEntityManager();      
        em.getTransaction().begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Long num = (Long) em.createQuery("select count(e.id) from Employee e").getSingleResult();
        detachListAppender(eclipselinkSqlLogger, listAppender);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        
        assertEquals(4, num.intValue());; 
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith("SELECT COUNT(ID) FROM employee"));
        em.close();
    }
    
    /**
     * Test getting the salary of employees.
     */
    @Test    
    public void _10_test_get_employee_salary() {          
        EntityManager em = emf.createEntityManager();      
        em.getTransaction().begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        List<Employee> employees = em.createQuery("select e from Employee e where e.salary is not null").getResultList();
        detachListAppender(eclipselinkSqlLogger, listAppender);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
               
        int num = employees.size();
        assertEquals(4, num);; 
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith("SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (SALARY IS NOT NULL)"));
       
        em.close();
    }
    
    /**
     * Test deleting all records in employee table.
     */
    @Test    
    public void _11_test_purge_employee_table() {          
        EntityManager em = emf.createEntityManager();      
        em.getTransaction().begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Employee> deleteAll = cb.createCriteriaDelete(Employee.class);  
        em.createQuery(deleteAll).executeUpdate();
        detachListAppender(eclipselinkSqlLogger, listAppender);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
                
        Long num = (Long) em.createQuery("select count(e.id) from Employee e").getSingleResult();
        assertTrue(num==0); 
        assertEquals(2, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(DELETE_EMP_PROJ_1));
        assertThat(loggingEvents.get(1).getMessage(),
                startsWith("DELETE FROM employee"));
        em.close();
    }

    /**
     * Database environment tear down, runs after class.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        logger.debug("oneTimeTearDown");
        if (emf != null) {
            emf.close();
        }
        if (server != null) {
            server.stop();
        }
    }

}