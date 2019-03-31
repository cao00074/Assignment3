/**********************************************************************egg*m******a******n********************
 * File: EmployeePhonesSuite.java
 * Course materials (19W) CST 8277
 * @author (original) Mike Norman
 * Modified by Chenxiao Cui and Lei Cao
 * @date 2019 03
 * Description: Test cases for the relationship between Employee and Project, including CRUD and other scenarios. 
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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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

    /**
     * Delcare test features - EntityManagerFactory and Server
     */
    public static EntityManagerFactory emf;
    public static Server server;
    
    /**
     * SQL statements for checking actual SQL for every test case.
     */
    private static final String COUNT_PROJECT = 
            "SELECT COUNT(ID) FROM project";
    private static final String COUNT_EMPLOYEE = 
            "SELECT COUNT(ID) FROM employee";
    private static final String COUNT_ADDRESS = 
            "SELECT COUNT(ID) FROM address";  
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
     * Test number of projects at start
     */
    @Test
    public void test_01_number_projects_at_start() {
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
    
    /**
     * Test number of employees at start
     */
    @Test
    public void test_02_number_employees_at_start() {
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
    
    /**
     * test number of addresses at start
     */
    @Test
    public void test_03_number_addresses_at_start() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Query query = em.createNamedQuery("countAddress");
        long count = (long) query.getSingleResult();
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);        
        assertTrue(count==3);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
                startsWith(COUNT_ADDRESS));
        em.close();
    }
    
    /**
     * Test number of projects that employee with PK 1 has
     */
    @Test
    public void test_04_numbe_of_projects_by_employee() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.begin();
        Employee emp = em.find(Employee.class, 1);
        detachListAppender(eclipselinkSqlLogger, listAppender);
        List<Project> projects = emp.getProjects();
        assertEquals(3,projects.size());
        List<ILoggingEvent> loggingEvents = listAppender.list;       
        assertEquals(3, loggingEvents.size());
        em.close();
    }
    
    /**
     * Test project with PK 1 only has one employee
     */
    @Test
    public void test_05_project_one_belongs_to_employee_one_only() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.begin();
        Project project1 = em.find(Project.class, 1);
        Employee employee1 = em.find(Employee.class, 1);
        detachListAppender(eclipselinkSqlLogger, listAppender);
        List<Employee> employees = project1.getEmployees();
        assertTrue(employees.size()==1);
        Employee empOwnsProject1 = employees.get(0);        
        assertEquals(employee1, empOwnsProject1);
        em.close();
    }
    
    /**
     * Test empployee with PK 2 does not have any project
     */
    @Test
    public void test_06_employee_two_has_no_project() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.begin();
        Employee employee2 = em.find(Employee.class, 2);        
        detachListAppender(eclipselinkSqlLogger, listAppender);
        List<Project> projects = employee2.getProjects();
        assertTrue(projects.size()==0);     
        em.close();
    }

    /**
     * test create a new project
     */
    @Test
    public void test_07_create_project() {
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
    /**
     * test updating an project's name
     */
    
    @Test
    public void test_08_update_project() {
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
    
    /**
     * Test deleting an project that has no employee
     */
    @Test
    public void test_09_delete_non_relational_project() {
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
    
    /**
     * Test adding one employee who has two new projects
     */
    @Test
    public void test_10_update_emp_proj_by_adding_one_emp_to_two_projs() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
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
        int numOfProjForEmp2 = employee2.getProjects().size();        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        Root<Employee> root = cq.from(Employee.class);
        cq.select(root);
        cq.where(cb.equal(root.get(Employee_.id), 2));
        TypedQuery<Employee> query = em.createQuery(cq);
        Employee employee2InDB = query.getSingleResult(); 
        assertEquals(employee2,employee2InDB);
        int numOfProjForEmp2InDB = employee2InDB.getProjects().size();
        assertTrue(numOfProjForEmp2==numOfProjForEmp2InDB);
        assertTrue(numOfProjForEmp2==2);        
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(3, loggingEvents.size());
        assertThat(loggingEvents.get(2).getMessage(),
                startsWith(INSERT_PROJECT_FROM_PROJECT));
        em.close();
    }
    
    /**
     * testing adding one relationship between Employee and Project
     */
    @Test
    public void test_11_update_emp_proj_by_adding_one_proj() {
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
    
    /**
     * Test deleting a project that used to belong to an employee
     */
    @Test
    public void test_12_delete_relational_project() {
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
    
    /**
     * Test updating a project's description
     */
    @Test
    public void test_13_update_proj_description() {
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
    
    /**
     * Test updating Emp_Proj table by assigning two employees to one project
     */
    @Test
    public void test_14_update_emp_proj_by_adding_one_proj_to_two_emps() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();        
        Project project3 = em.find(Project.class, 3);
        Employee employee2 = em.find(Employee.class, 2);
        Employee employee3 = em.find(Employee.class, 3);       
        List<Employee> employees = new ArrayList<Employee>();
        employees.add(employee2);
        employees.add(employee3);
        project3.setEmployees(employees);
        em.persist(project3);
        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        tx.commit();
        detachListAppender(eclipselinkSqlLogger, listAppender);       
        Project newProject3 = em.find(Project.class, 3);
        assertTrue(newProject3.getEmployees().size()==2);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
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
