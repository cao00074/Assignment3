/**********************************************************************egg*m******a******n********************
 * File: EmployeeTestSuite.java
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
public class EmployeeTestSuite implements TestSuiteConstants {

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

    private static final String SELECT_EMPLOYEE_1 =
        "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM employee WHERE (ID = ?)";
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

    // C-R-U-D lifecycle
    @Test
  //  @Ignore //remove this when TODO is done
    public void _02_test_create_employee() {
        Employee employee1 = new Employee();
        employee1.setId(1);
        employee1.setFirstName("FirstName");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(employee1);
        em.getTransaction().commit();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> rootEmpQuery = cq.from(Employee.class);
        cq.select(rootEmpQuery);
        cq.where(cb.and(cb.equal(rootEmpQuery.get(Employee_.id),1)));
        TypedQuery<Employee> query =em.createQuery(cq);
        Employee employeeFromDB = query.getSingleResult();
        
        System.out.println("id: "+employeeFromDB.getId()+" FN: "+employeeFromDB.getFirstName());
        
        assertTrue(employeeFromDB.getFirstName()=="FirstName");
        em.close();
    }
    
    @Test
    public void _03_test_read_employee() {
        EntityManager em = emf.createEntityManager();
        Employee employee = (Employee) em.createQuery("select e from Employee e where e.id = 1")
                                         .getSingleResult();
        assertNotNull(employee);        
    }
    
    @Test
    public void _04_test_update_employee() {
               
        EntityManager em = emf.createEntityManager();
        Employee employee1 = em.find(Employee.class, 1);
        employee1.setFirstName("UpdatedFirstName");
        em.getTransaction().begin();
        em.persist(employee1);
        em.getTransaction().commit();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> rootEmpQuery = cq.from(Employee.class);
        cq.select(rootEmpQuery);
        cq.where(cb.and(cb.equal(rootEmpQuery.get(Employee_.id),1)));
        TypedQuery<Employee> query =em.createQuery(cq);
        Employee employeeFromDB = query.getSingleResult();
       // System.out.println("id: "+employeeFromDB.getId()+" FN: "+employeeFromDB.getFirstName());
        
        assertEquals("UpdatedFirstName", employeeFromDB.getFirstName());
        em.close();
    }
    
    @Test
    public void _05_test_delete_employee() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createNamedQuery("deleteEmployee").executeUpdate();
        em.getTransaction().commit();
        Employee employee = em.find(Employee.class,1);
       // System.out.println("id: "+employee.getId()+" FN: "+employee.getFirstName()+" result: "+result);
        assertNull(employee);        
    }
    
    @Test
    public void _06_test_delete_employee2() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Employee employeeNew = new Employee();
        employeeNew.setId(2);
        employeeNew.setLastName("Cui");
        em.persist(employeeNew);
        em.getTransaction().commit();
        Employee employee = em.find(Employee.class,2);
        assertNotNull(employee);
        em.getTransaction().begin();
        em.remove(employee);
        em.getTransaction().commit();
        employee = em.find(Employee.class,2);
       // System.out.println("id: "+employee.getId()+" FN: "+employee.getFirstName()+" result: "+result);
        assertNull(employee);        
    }

    // queries - highest salary?
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
        Employee highestSalary =(Employee)em.createNamedQuery("findHighestSalary").getSingleResult();
        
        assertTrue(highestSalary.getSalary()==1000);      
        em.close();
    }
    
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
        List<Employee> sameLastName =(List<Employee>)em.createNamedQuery("findEmployeesWithLastName").setParameter("lastName", "Li").getResultList();
        
        assertTrue(sameLastName.size()==2);        
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

}