/**********************************************************************egg*m******a******n********************
 * File: EmployeeAddressTestSuite.java
 * Course materials (19W) CST 8277
 * @author (original) Mike Norman
 *
 *
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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Metamodel;

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

    // test fixture(s)
    public static EntityManagerFactory emf;
    public static Server server;

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

    // TODO - add test cases
    private static final String SELECT_ADDRESS =
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE (ID = ?)";
    private static final String INSERT_ADDRESS =
            "INSERT INTO address (ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_EMPLOYEE_WITH_ADDRESS_ID =
            "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (ADDR_ID = ?)";    
    private static final String SELECT_EMPLOYEE_WITH_EMPLOYEE_ID =
            "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (ID = ?)";
    private static final String UPDATE_ADDRESS = 
            "UPDATE address SET STREET = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String UPDATE_EMPLOYEE =
           "UPDATE employee SET ADDR_ID = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_EMPLOYEE_FROM_EMP_PROJ = 
            "DELETE FROM EMP_PROJ WHERE (EMP_ID = ?)";
    private static final String DELETE_EMPLOYEE = 
            "DELETE FROM employee WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_ADDRESS = 
            "DELETE FROM address WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String INSERT_EMPLOYEE =
            "INSERT INTO employee (ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID) VALUES (?, ?, ?, ?, ?, ?)";   
    private static final String COUNT_ADDRESS = 
            "SELECT COUNT(ID) FROM address";
    private static final String SELECT_EMPLOYEE_BY_POSTAL = 
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE POSTAL LIKE ?";
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
    
    @Test
    public void test_03_find_employee_by_postal() {
        EntityManager em = emf.createEntityManager();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);         
        Query query = em.createNamedQuery("findAllEmployeesWithPostal");
        query.setParameter("postal", "K2X 5A1");
        Address addr = (Address) query.getSingleResult();
        Employee emp = addr.getEmployee();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        assertTrue(emp.getFirstName().equals("Mike"));
        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(SELECT_EMPLOYEE_BY_POSTAL));
     
    }
    
    @Test
    public void test_04_insert_address_belongs_to_employee() {
        
        EntityManager em = emf.createEntityManager();
        

        //insert query
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
    
    @Test
    public void test_05_total_address() {
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
    
    @Test
    public void test_06_delete_non_relational_address() {
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
 
    @Test
    public void test_07_insert_employee() {        
        EntityManager em = emf.createEntityManager();
       
        //insert an employee with addr_id "1"
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
    
    @Test
    public void test_08_update_address() {
        
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
    

    @Test
    public void test_09_update_employee() {
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
   
    
    @Test
    public void test_10_delete_relational_address() {
        EntityManager em = emf.createEntityManager();

       
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Address addr = em.find(Address.class,3);             //em.find Address with PK 1
        em.remove(addr);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();        
        detachListAppender(eclipselinkSqlLogger, listAppender);  
        addr = em.find(Address.class, 3);
      
        assertNull(addr);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(3, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(DELETE_EMPLOYEE_FROM_EMP_PROJ));
        assertThat(loggingEvents.get(1).getMessage(),
                startsWith(DELETE_EMPLOYEE));     
        assertThat(loggingEvents.get(2).getMessage(),
                startsWith(DELETE_ADDRESS));
        em.close();
    }
    

    
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

}//    @Test
//    //  @Ignore //remove this when TODO is done
//      public void _01_test_create_address() {
//          Address address1 = new Address();
//          address1.setCity("Ottawa");
//          EntityManager em = emf.createEntityManager();
//          em.getTransaction().begin();
//          em.persist(address1);
//          em.getTransaction().commit();
//          CriteriaBuilder cb = em.getCriteriaBuilder();
//          CriteriaQuery<Address> cq = cb.createQuery(Address.class);
//          Root<Address> rootEmpQuery = cq.from(Address.class);
//          cq.select(rootEmpQuery);
//          cq.where(cb.and(cb.equal(rootEmpQuery.get(Address_.city),"Ottawa")));
//          TypedQuery<Address> query =em.createQuery(cq);
//          Address addressFromDB = query.getSingleResult();
//          
//          System.out.println("id: "+addressFromDB.getId()+" FN: "+addressFromDB.getCity());
//          
//          assertTrue(addressFromDB.getId()==2);
//          em.close();
//      }

 