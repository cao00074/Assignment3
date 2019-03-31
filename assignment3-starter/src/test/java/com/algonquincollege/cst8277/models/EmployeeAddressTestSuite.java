/**********************************************************************egg*m******a******n********************
 * File: EmployeeAddressTestSuite.java
 * Course materials (19W) CST 8277
 * @author (original) Mike Norman
 * Modified by Chenxiao Cui and Lei Cao
 * @date 2019 03
 *Description: Test cases for the relationship between Employee and Address, including CRUD and other scenarios. 
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

import org.junit.runners.MethodSorters;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeeAddressTestSuite implements TestSuiteConstants {

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
     * SQL statements for checking actual SQL for every test case.
     */
    private static final String SELECT_ADDRESS =
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE (ID = ?)";
    private static final String INSERT_ADDRESS =
            "INSERT INTO address (ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_EMPLOYEE_WITH_ADDRESS_ID =
            "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (ADDR_ID = ?)";    
    private static final String UPDATE_ADDRESS = 
            "UPDATE address SET STREET = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String UPDATE_EMPLOYEE =
           "UPDATE employee SET ADDR_ID = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_ADDRESS = 
            "DELETE FROM address WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String INSERT_EMPLOYEE =
            "INSERT INTO employee (ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID) VALUES (?, ?, ?, ?, ?, ?)";   
    private static final String COUNT_ADDRESS = 
            "SELECT COUNT(ID) FROM address";
    private static final String SELECT_EMPLOYEE_BY_POSTAL = 
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE POSTAL LIKE ?";
    private static final String SELECT_EMPLOYEE_BY_STREET = 
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE STREET LIKE ?";
    private static final String SELECT_EMPLOYEE_IN_NEPEAN = 
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE CITY LIKE ?";   
    
    /**
     * Test Address table is not empty at start
     */
    @Test
    public void test_01_address_not_empty_at_start() {
        EntityManager em = emf.createEntityManager();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Address addr = em.find(Address.class,1);             //em.find Address with PK 1 and any its dependent entity
        detachListAppender(eclipselinkSqlLogger, listAppender);
        assertNotNull(addr);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(2, loggingEvents.size());        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_ADDRESS));
        assertThat(loggingEvents.get(1).getMessage(),
                startsWith(SELECT_EMPLOYEE_WITH_ADDRESS_ID));
        em.close();
    }
    
    /**
     * Test address with PK 1 belongs to an employee
     */
    @Test 
    public void test_02_address_belongs_to_an_employee() {
        EntityManager em = emf.createEntityManager();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Address addr = em.find(Address.class,1);             //em.find Address with PK 1 and any its dependent entity
        detachListAppender(eclipselinkSqlLogger, listAppender);
        Employee emp = addr.getEmployee();
        assertNotNull(emp);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(0, loggingEvents.size());
        em.close();
    }
    
     /**
      * Test finding an employee by its postal code using JPQL 
      */
    @Test
    public void test_03_find_employee_by_postal() {
        EntityManager em = emf.createEntityManager();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);         
        Query query = em.createNamedQuery("findEmployeesWithPostal");
        query.setParameter("postal", "K2X 5A1");
        Address addr = (Address) query.getSingleResult();
        Employee emp = addr.getEmployee();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        assertTrue(emp.getFirstName().equals("Mike"));       
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(SELECT_EMPLOYEE_BY_POSTAL));
        em.close();
     
    }
    /**
     * Test no employee lives on 185 Charlies Lane using JPQL
     */
   @Test
   public void test_04_find_employee_by_street() {
       EntityManager em = emf.createEntityManager();
       ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);         
       Query query = em.createNamedQuery("findEmployeesByStreet");
       query.setParameter("street", "185 Charlies Lane");
       Address addr = (Address) query.getSingleResult();
       Employee emp = addr.getEmployee();
       detachListAppender(eclipselinkSqlLogger, listAppender);
       assertNull(emp);       
       List<ILoggingEvent> loggingEvents = listAppender.list;
       assertEquals(2, loggingEvents.size());
       assertThat(loggingEvents.get(0).getMessage(),
               startsWith(SELECT_EMPLOYEE_BY_STREET));
       em.close();
   }
   
   /**
    * Test finding the only employee who lives in Nepean
    */
   @Test
   public void test_05_find_employee_lives_in_nepean() {
       EntityManager em = emf.createEntityManager();
       ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);         
       Query query = em.createNamedQuery("findEmployeesInNepean");
       query.setParameter("city", "Nepean");
       Address addr = (Address) query.getSingleResult();
       Employee emp = addr.getEmployee();
       detachListAppender(eclipselinkSqlLogger, listAppender);
       assertTrue(emp.getFirstName().equals("Mike"));       
       List<ILoggingEvent> loggingEvents = listAppender.list;
       assertEquals(1, loggingEvents.size());
       assertThat(loggingEvents.get(0).getMessage(),
               startsWith(SELECT_EMPLOYEE_IN_NEPEAN));
       em.close();
   }
    
    /**
     * Test inserting relational address to Address table using insert query
     */
    @Test
    public void test_06_insert_address_belongs_to_employee() {        
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Query query = em.createNativeQuery(INSERT_ADDRESS);        
        query.setParameter(1, 2);
        query.setParameter(2, "Ottawa");
        query.setParameter(3, "Canada");
        query.setParameter(4, "koa1l0");
        query.setParameter(5, "Ontario"); 
        query.setParameter(6, "185 Charlie's Lane");
        query.setParameter(7, 3);
        query.executeUpdate();
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        Address addr = em.find(Address.class,2);
        Employee emp = addr.getEmployee();            
        assertNotNull(addr);
        assertNull(emp);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(INSERT_ADDRESS));
        em.close();
    }
    
    /**
     * Test counting total number of employees in Employee table
     */
    @Test
    public void test_07_total_address() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Query query = em.createNamedQuery("countAddress");
        long count = (long) query.getSingleResult();
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);            
        assertEquals(3, count);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());       
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(COUNT_ADDRESS));   
        em.close();
    }
    
    /**
     * Test deleting non relational addess from Address table using "remove" by EntityManager
     */
    @Test
    public void test_08_delete_non_relational_address() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Address addr = em.find(Address.class,2);             //em.find Address with PK 2
        em.remove(addr);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);  
        addr = em.find(Address.class, 2);     
        assertNull(addr);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(DELETE_ADDRESS));
        em.close();
    }
 
    /**
     * Test inserting an employee to Employee table using insert query
     */
    @Test
    public void test_09_insert_employee() {        
        EntityManager em = emf.createEntityManager();       
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Query query = em.createNativeQuery(INSERT_EMPLOYEE);        
        query.setParameter(1, 2); // employee id
        query.setParameter(2, "Calvin");
        query.setParameter(3, "Klein");
        query.setParameter(4, "90000");
        query.setParameter(5, 3);  //version
        query.setParameter(6, 1); //address id
        query.executeUpdate();       
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);        
        Employee emp = em.find(Employee.class,2);
        Address addr = em.find(Address.class, 1);       
        assertEquals(emp.getAddress(),addr);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(INSERT_EMPLOYEE)); 
        em.close();
    }   
    
    /**
     * test updating an address description in the Address table 
     */
    @Test
    public void test_10_update_address() {        
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Address addr = em.find(Address.class, 1);
        addr.setStreet("28 Pinetrail Cress");
        Employee emp = em.find(Employee.class, addr.getEmployee().getId());        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();         
        detachListAppender(eclipselinkSqlLogger, listAppender);
        Address newAddress = emp.getAddress();
        assertEquals("28 Pinetrail Cress", newAddress.getStreet());
        assertEquals(emp.getAddress(),addr);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(UPDATE_ADDRESS)); 
        em.close();
    }
    
    /**
     * test updating Employee table by assigning an address to an employee
     */
    @Test
    public void test_11_update_employee() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Employee emp = em.find(Employee.class, 1);
        Address addr = em.find(Address.class, 3);       
        emp.setAddress(addr);        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        em.refresh(addr);
        assertEquals(addr.getEmployee(), emp);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(UPDATE_EMPLOYEE));
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

 