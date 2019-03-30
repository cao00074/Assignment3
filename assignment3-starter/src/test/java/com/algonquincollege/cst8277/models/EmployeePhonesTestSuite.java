/**********************************************************************egg*m******a******n********************
 * File: EmployeePhonesSuite.java
 * Course materials (19W) CST 8277
 * @author (original) Mike Norman
 * Modified by Chenxiao Cui and Lei Cao
 * @date 2019 03
 * Description: Test cases for the relationship between Employee and Phone, including CRUD and other scenarios. 
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
public class EmployeePhonesTestSuite implements TestSuiteConstants {

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
    private static final String SELECT_EMPLOYEE =
            "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (ID = ?)";
    private static final String SELECT_ADDRESS =
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE (ID = ?)";
    private static final String CREATE_PHONE =
            "INSERT INTO phone (AREACODE, PHONENUMBER, TYPE, VERSION, OWNING_EMP_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_PHONE1 =
            "SELECT ID, AREACODE, PHONENUMBER, TYPE, VERSION, OWNING_EMP_ID FROM phone WHERE (ID = ?)";
    private static final String SELECT_PHONE2 =
            "SELECT ID, AREACODE, PHONENUMBER, TYPE, VERSION, OWNING_EMP_ID FROM phone WHERE PHONENUMBER LIKE ?";
    private static final String UPDATE_PHONE =
            "UPDATE phone SET TYPE = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String UPDATE_EMPLOYEE =
            "UPDATE employee SET LASTNAME = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_PHONE =
            "DELETE FROM phone WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_EMP_PROJ =
            "DELETE FROM EMP_PROJ WHERE (EMP_ID = ?)";
    private static final String DELETE_EMPLOYEE =
            "DELETE FROM employee WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String DELETE_PHONE_ALL =
            "DELETE FROM phone";
    
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
     * Test Employee table is not empty at the beginning.
     */
    @Test
    public void _01_test_employee_not_empty_at_start() {
        EntityManager em = emf.createEntityManager();

        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Employee emp1 = em.find(Employee.class,10);             //em.find Employee with PK 1
        detachListAppender(eclipselinkSqlLogger, listAppender);

        assertNotNull(emp1);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(3, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_EMPLOYEE));
        assertThat(loggingEvents.get(1).getMessage(),
            startsWith(SELECT_ADDRESS));
        
        em.close();
    }
    
    /**
     * Test Phone table is empty at the beginning.
     */
    @Test
    public void _02_test_phone_empty_at_start() {
        EntityManager em = emf.createEntityManager();

        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Phone p1 = em.find(Phone.class,1);             //em.find Employee with PK 1
        detachListAppender(eclipselinkSqlLogger, listAppender);

        assertNull(p1);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_PHONE1));        
        em.close();
    }

    /**
     * Test creating a phone for a certain employee (inserting a row in phone table).
     */
    @Test
    public void _03_test_create_one_phone_to_employee() {        
        EntityManager em = emf.createEntityManager();
        
        Employee employee = em.find(Employee.class,10); 
        Phone phone1 = new Phone();
        phone1.setPhoneNumber("613-123-4567");
        phone1.setEmployee(employee);
        phone1.setVersion(1);
        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().begin();
        em.persist(phone1);
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
        Root<Phone> rootPhoneQuery = cq.from(Phone.class);
        cq.select(rootPhoneQuery);
        cq.where(cb.and(cb.equal(rootPhoneQuery.get(Phone_.id),1)));
        TypedQuery<Phone> query =em.createQuery(cq);
        Phone phoneFromDB = query.getSingleResult();        
       
        assertTrue(phoneFromDB.getPhoneNumber().equalsIgnoreCase("613-123-4567"));
        List<ILoggingEvent> loggingEvents = listAppender.list;        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(CREATE_PHONE));
        em.close();
    }
    
    /**
     * Test creating 2 phones for a certain employee (inserting 2 rows in phone table).
     */
    @Test
    public void _04_test_create_two_phones_to_employee() {        
        EntityManager em = emf.createEntityManager();
        
        Employee employee = em.find(Employee.class,20); 
        Phone phone1 = new Phone();
        phone1.setPhoneNumber("343-123-4567");
        phone1.setEmployee(employee);
        phone1.setVersion(1);
       
        Phone phone2 = new Phone();
        phone2.setPhoneNumber("343-789-2345");
        phone2.setEmployee(employee);
        phone2.setVersion(1);
        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().begin();
        em.persist(phone1);
        em.persist(phone2);
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        assertEquals(2, employee.getPhones().size());    
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(4, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(CREATE_PHONE));
        assertThat(loggingEvents.get(1).getMessage(),
            startsWith("CALL IDENTITY()"));
        assertThat(loggingEvents.get(2).getMessage(),
            startsWith(CREATE_PHONE));
        assertThat(loggingEvents.get(3).getMessage(),
            startsWith("CALL IDENTITY()"));
        
        em.close();
    }
    
    /**
     * Test getting a phone from database with id
     */
    @Test
    public void _05_test_read_phone() {        
        EntityManager em = emf.createEntityManager();
        Phone phone = em.find(Phone.class,1); 
       
        assertNotNull(phone);
        assertEquals("613-123-4567", phone.getPhoneNumber());
        em.close();
    }
    
    /**
     * Test getting phones of a certain employee
     */
    @Test
    public void _06_test_read_phone_by_employee() {        
        EntityManager em = emf.createEntityManager();
        Employee employee = em.find(Employee.class, 20);        
        List<Phone> phones = employee.getPhones();    
        
        assertEquals(2, phones.size());
        em.close();
    }
    
    /**
     * Test getting the employee who has the phone
     */
    @Test
    public void _07_test_read_empoyee_by_phone() {        
        EntityManager em = emf.createEntityManager();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Phone phone = (Phone) em.createNamedQuery("findPhoneWithNumber").setParameter("phoneNumber","343-123-4567").getSingleResult();
        Employee employee = phone.getEmployee();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        assertEquals(20, employee.id);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_PHONE2));
        em.close();
    }
    
    /**
     * Test updating a phone (updating a row in phone table).
     */
    @Test
    public void _08_test_update_phone() {        
        EntityManager em = emf.createEntityManager();
        
        Phone phone = em.find(Phone.class,1);   
        phone.setType("New Type");
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().begin();
        em.persist(phone);
        em.getTransaction().commit();  
        detachListAppender(eclipselinkSqlLogger, listAppender);
             
        Phone phone2 = (Phone) em.createQuery("Select p from Phone p where p.id = 1")
                .getSingleResult();
        
        assertEquals("New Type", phone2.getType());
        assertEquals(2, phone.getVersion());
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(UPDATE_PHONE));
        em.close();
    }
    
    /**
     * Test updating an employee (updating a row in employee table).
     */
    @Test
    public void _09_test_update_employee() {        
        EntityManager em = emf.createEntityManager();
        
        Employee employee = em.find(Employee.class, 10); 
        employee.setLastName("New Last Name");
        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();  
        detachListAppender(eclipselinkSqlLogger, listAppender);
        em.refresh(employee);          
        
        assertEquals("New Last Name", employee.getLastName());
        assertEquals(2, employee.getVersion());
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(UPDATE_EMPLOYEE));
        em.close();
    }
    
    /**
     * Test getting the numbers of phones.
     */
    @Test    
    public void _10_test_count_phones() {          
        EntityManager em = emf.createEntityManager();      
        em.getTransaction().begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Long num = (Long) em.createQuery("select count(p.id) from Phone p").getSingleResult();
        detachListAppender(eclipselinkSqlLogger, listAppender);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        
        assertEquals(3, num.intValue());; 
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith("SELECT COUNT(ID) FROM phone"));
        em.close();
    }
    
    /**
     * Test deleting a phone with id.
     */
    @Test
    public void _11_test_delete_phone() {        
        EntityManager em = emf.createEntityManager();
        Phone phone = em.find(Phone.class, 3);
        assertNotNull(phone);
        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().begin();
        em.remove(phone);
        em.getTransaction().commit();        
        detachListAppender(eclipselinkSqlLogger, listAppender);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        phone = em.find(Phone.class, 3);

        Employee employee = em.find(Employee.class, 20);
        em.refresh(employee);                
        assertNull(phone);         
        assertEquals(1, loggingEvents.size());        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(DELETE_PHONE));
        em.close();
    }
    
    /**
     * Test deleting a phone of an employee.
     */
    @Test
    public void _12_test_delete_phone_with_emp() {        
        EntityManager em = emf.createEntityManager();
        Employee employee = em.find(Employee.class, 10);
        System.out.println(employee.getPhones().get(0).getId());
        int howManyPhones = employee.getPhones().size();
        
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().begin();
        Phone phone = em.find(Phone.class,1);
        em.remove(phone);
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        Phone deletedPhone = em.find(Phone.class, 1);
        em.refresh(employee);
        
        assertNull(deletedPhone); 
        assertEquals(howManyPhones-1, employee.getPhones().size());
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(DELETE_PHONE));
        em.close();
    }
    
    /**
     * Test deleting an employee.
     */
    @Test
    public void _13_test_delete_employee() {        
        EntityManager em = emf.createEntityManager();
        Phone phone = em.find(Phone.class,2);
        int whoHasThisPhone = phone.getEmployee().getId();
        Employee employee = em.find(Employee.class, whoHasThisPhone);        
        em.getTransaction().begin();  
        em.remove(employee);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        em.getTransaction().commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        employee = em.find(Employee.class, whoHasThisPhone);
               
        assertNull(employee);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(3, loggingEvents.size());
        
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(DELETE_EMP_PROJ));
        assertThat(loggingEvents.get(1).getMessage(),
            startsWith(DELETE_PHONE));
        assertThat(loggingEvents.get(2).getMessage(),
            startsWith(DELETE_EMPLOYEE));
        em.close();
    }
    
    /**
     * Test deleting all records in phone table.
     */
    @Test
    public void _14_test_purge_phone_table() {        
        EntityManager em = emf.createEntityManager();      
        em.getTransaction().begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Phone> deleteAll = cb.createCriteriaDelete(Phone.class);  
        em.createQuery(deleteAll).executeUpdate();
        detachListAppender(eclipselinkSqlLogger, listAppender);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
                
        Long num = (Long) em.createQuery("select count(p.id) from Phone p").getSingleResult();
        assertTrue(num==0); 
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(DELETE_PHONE_ALL));
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
