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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.runners.MethodSorters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.junit.Test;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeeProjectsTestSuite implements TestSuiteConstants {

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
    private static final String COUNT_PROJECT = 
            "SELECT COUNT(ID) FROM project";
    private static final String COUNT_EMPLOYEE = 
            "SELECT COUNT(ID) FROM employee";
    private static final String SELECT_PROJECT = 
            "SELECT ID, DESCRIPTION, NAME, VERSION FROM project WHERE (ID = ?)";
    private static final String INSERT_PROJECT = 
            "INSERT INTO project (DESCRIPTION, NAME, VERSION) VALUES (?, ?, ?)";
    private static final String UPDATE_PROJECT = 
            "UPDATE project SET NAME = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String UPDATE_PROJECT_DESCRIPTION = 
            "UPDATE project SET DESCRIPTION = ?, VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    
    private static final String DELETE_PROJECT_FROM_EMP_PROJ = 
            "DELETE FROM EMP_PROJ WHERE (PROJ_ID = ?)";
    private static final String DELETE_PROJECT_FROM_PROJECT = 
            "DELETE FROM project WHERE ((ID = ?) AND (VERSION = ?))";
    private static final String INSERT_PROJECT_FROM_PROJECT = 
            "INSERT INTO EMP_PROJ (PROJ_ID, EMP_ID) VALUES (?, ?)";
    private static final String UPDATE_EMPLOYEE =
            "UPDATE employee SET VERSION = ? WHERE ((ID = ?) AND (VERSION = ?))";
    @Test
    public void test_01_number_project_at_start() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Query query = em.createNamedQuery("countProject");
        long count = (long) query.getSingleResult();
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        assertTrue(count==3);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(COUNT_PROJECT));
        em.close();
    }
    
    @Test
    public void test_02_number_employee_at_start() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Query query = em.createNamedQuery("countEmployee");
        long count = (long) query.getSingleResult();
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        assertTrue(count==3);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(COUNT_EMPLOYEE));
        em.close();
    }
    
    @Test
    public void test_03_numbe_of_projects_by_employee() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.begin();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        Employee emp = em.find(Employee.class, 1);
        List<Project> projects = emp.getProjects();
        assertEquals(3,projects.size());
        em.close();
    }

    @Test
    public void test_04_create_project() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Project newProject = new Project();
        newProject.setName("Assignment4");
        em.persist(newProject);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();        
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        Project project4 = em.find(Project.class, 4);
        assertEquals(project4, newProject);
        assertEquals("Assignment4", project4.getName());
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(2, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(INSERT_PROJECT));
        em.close();
    }
    
    @Test
    public void test_05_update_project() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Project project = em.find(Project.class, 4);
        project.setName("Lab4");
        em.persist(project);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();        
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        Project newProject = em.find(Project.class, 4);
        assertEquals("Lab4", newProject.getName());
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(UPDATE_PROJECT));
        em.close();
    }
    
    @Test
    public void test_06_delete_non_relational_project() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Project project = em.find(Project.class, 4);
        em.remove(project);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        project = em.find(Project.class, 4);
        
        assertNull(project);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(2, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(DELETE_PROJECT_FROM_EMP_PROJ));
        assertThat(loggingEvents.get(1).getMessage(),
                startsWith(DELETE_PROJECT_FROM_PROJECT));
        em.close();
    }
    
    @Test
    public void test_07_update_emp_proj_by_addng_emp_to_proj() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
       
        Employee employee1 = em.find(Employee.class, 1);
        Employee employee2 = em.find(Employee.class, 2); 
        
        Project project1 = em.find(Project.class, 1);
        Project project2 = em.find(Project.class, 2);
        
        List<Project> projects = new ArrayList<Project>();
        projects.add(project1);
        projects.add(project2);
        employee2.setProjects(projects);
        em.persist(employee2);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        int numOfProjForEmp1 = employee1.getProjects().size();
        assertEquals(3, numOfProjForEmp1);
        int numOfProjForEmp2 = employee2.getProjects().size();
        assertTrue(numOfProjForEmp2==2);

        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(4, loggingEvents.size());
        assertThat(loggingEvents.get(2).getMessage(),
                startsWith(INSERT_PROJECT_FROM_PROJECT));
        assertThat(loggingEvents.get(3).getMessage(),
                startsWith(INSERT_PROJECT_FROM_PROJECT));
        em.close();
    }
    
    @Test
    public void test_08_update_emp_proj_by_adding_proj_to_emp() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
       
        Employee employee3 = em.find(Employee.class, 3);
        Project project2 = em.find(Project.class, 2);
        List<Project> projects = new ArrayList<Project>();
        projects.add(project2);
        employee3.setProjects(projects);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        em.refresh(employee3);
        
        int numOfProjForEmp3 = employee3.getProjects().size();
        assertEquals(1, numOfProjForEmp3);
        int numOfEmpOfProj2 = project2.getEmployees().size();
        assertTrue(numOfEmpOfProj2==3);

        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(3, loggingEvents.size());
        assertThat(loggingEvents.get(1).getMessage(),
                startsWith(UPDATE_EMPLOYEE));       
        assertThat(loggingEvents.get(2).getMessage(),
                startsWith(INSERT_PROJECT_FROM_PROJECT));

        em.close();
    }   
    
    @Test
    public void test_09_delete_relational_project() {
        EntityManager em = emf.createEntityManager();EntityTransaction tx = em.getTransaction();
        tx.begin();        
        final Project project2 = em.find(Project.class, 2);  
        project2.getEmployees().forEach( e -> e.getProjects().remove(project2));
        
        em.remove(project2);

        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        
        Employee employee = em.find(Employee.class, 2);

        Project project2X = em.find(Project.class, 2); 
        assertNull(project2X);
        assertThat(employee.getProjects(), hasSize(1));
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(8, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(DELETE_PROJECT_FROM_EMP_PROJ));
        em.close();
    }
    
    @Test
    public void test_10_update_proj_description() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        Project project1 = em.find(Project.class, 1);
        project1.setDescription("CRUD and Hibernate");
        Employee employee2 = em.find(Employee.class, 2);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);
        em.refresh(employee2);        
        List<Project> projList = new ArrayList<Project>();
        projList = employee2.getProjects();
        Project project = projList.get(0);
        assertEquals("CRUD and Hibernate", project.getDescription());
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(UPDATE_PROJECT_DESCRIPTION));
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
