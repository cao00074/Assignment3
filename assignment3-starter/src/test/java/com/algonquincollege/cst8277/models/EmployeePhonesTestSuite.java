/**********************************************************************egg*m******a******n********************
 * File: EmployeePhonesSuite.java
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

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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

    // test fixture(s)
    public static EntityManagerFactory emf;
    public static Server server;

    private static final String SELECT_EMPLOYEE =
            "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (ID = ?)";
    private static final String SELECT_ADDRESS =
            "SELECT ID, CITY, COUNTRY, POSTAL, STATE, STREET, VERSION FROM address WHERE (ID = ?)";
    
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

    @Test
    public void _02_test_create_one_phone_to_employee() {        
        EntityManager em = emf.createEntityManager();
        
        Employee employee = em.find(Employee.class,10); 
        Phone phone1 = new Phone();
        phone1.setPhoneNumber("613-123-4567");
        phone1.setEmployee(employee);
        phone1.setVersion(1);
        
        em.getTransaction().begin();
        em.persist(phone1);
        em.getTransaction().commit();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
        Root<Phone> rootPhoneQuery = cq.from(Phone.class);
        cq.select(rootPhoneQuery);
        cq.where(cb.and(cb.equal(rootPhoneQuery.get(Phone_.id),1)));
        TypedQuery<Phone> query =em.createQuery(cq);
        Phone phoneFromDB = query.getSingleResult();
        
        System.out.println("Employee: "+phoneFromDB.getEmployee().getFirstName()+" NO.: "+phoneFromDB.getPhoneNumber());
        
       
        assertTrue(phoneFromDB.getPhoneNumber().equalsIgnoreCase("613-123-4567"));
        em.close();
    }
    
    @Test
    public void _03_test_create_two_phones_to_employee() {        
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
        
        em.getTransaction().begin();
        em.persist(phone1);
        em.persist(phone2);
        em.getTransaction().commit();
        
        assertEquals(2, employee.getPhones().size());        
        em.close();
    }
     
    @Test
    public void _04_test_read_phone() {        
        EntityManager em = emf.createEntityManager();
        
        Phone phone = em.find(Phone.class,1);       
       
        assertNotNull(phone);
        assertEquals("613-123-4567", phone.getPhoneNumber());
        em.close();
    }
    
    @Test
    public void _05_test_read_phone_by_employee() {        
        EntityManager em = emf.createEntityManager();
        Employee employee = em.find(Employee.class, 20);
        
        List<Phone> phones = employee.getPhones();      
        
        assertEquals(2, phones.size());
        em.close();
    }
    
    @Test
    public void _06_test_read_empoyee_by_phone() {        
        EntityManager em = emf.createEntityManager();
        Phone phone = (Phone) em.createNamedQuery("findPhoneWithNumber").setParameter("phoneNumber","343-123-4567").getSingleResult();
        Employee employee = phone.getEmployee();
        
        assertEquals(20, employee.id);
        em.close();
    }
    
    @Test
    public void _07_test_update_phone() {        
        EntityManager em = emf.createEntityManager();
        
        Phone phone = em.find(Phone.class,1);   
        phone.setType("New Type");
       
        em.getTransaction().begin();
        em.persist(phone);
        em.getTransaction().commit();        
             
        Phone phone2 = (Phone) em.createQuery("Select p from Phone p where p.id = 1")
                .getSingleResult();
        
        assertEquals("New Type", phone2.getType());
        assertEquals(2, phone.getVersion());
        em.close();
    }
    
    @Test
    public void _08_test_update_employee() {        
        EntityManager em = emf.createEntityManager();
        
        Employee employee = em.find(Employee.class, 10); 
        employee.setLastName("New Last Name");
       
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();        
        em.refresh(employee);     
       
        
        assertEquals("New Last Name", employee.getLastName());
        assertEquals(2, employee.getVersion());
        em.close();
    }
    
    @Test
    public void _09_test_delete_phone() {        
        EntityManager em = emf.createEntityManager();
        Employee employee = em.find(Employee.class, 10);
        int howManyPhones = employee.getPhones().size();
        
        em.getTransaction().begin();
        Phone phone = em.find(Phone.class,1);
        em.remove(phone);
        em.getTransaction().commit();
        
        Phone deletedPhone = em.find(Phone.class, 1);
        em.refresh(employee);
        
        assertNull(deletedPhone); 
        assertEquals(howManyPhones-1, employee.getPhones().size());
        em.close();
    }
    
    
    @Test
    public void _10_test_delete_employee() {        
        EntityManager em = emf.createEntityManager();
        Phone phone = em.find(Phone.class,2);
        int whoHasThisPhone = phone.getEmployee().getId();
       
        em.getTransaction().begin();
        Employee employee = em.find(Employee.class, whoHasThisPhone);
        em.remove(employee);
        em.getTransaction().commit();
        employee = em.find(Employee.class, whoHasThisPhone);
        phone = em.find(Phone.class,2);
               
        assertNull(phone);
        assertNull(employee);
        em.close();
    }
    
//    @Test
//    public void _11_test_get_all_phones_with_employee() {        
//        EntityManager em = emf.createEntityManager();
//        Employee employee = em.find(Employee.class, 20);
//        List<Phone> phones = em.createNamedQuery("findAllPhoneWithEmployee").setParameter("employee", employee).getResultList();
//        
//        assertEquals(2, phones.size());        
//        em.close();
//    }
    
    

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
