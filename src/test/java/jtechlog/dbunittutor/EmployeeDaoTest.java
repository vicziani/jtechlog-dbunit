package jtechlog.dbunittutor;

import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.hsqldb.jdbc.JDBCDataSourceFactory;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmployeeDaoTest {

    static DataSource dataSource;

    static EntityManagerFactory emf;

    EntityManager entityManager;

    EmployeeDao employeeDao;

    @BeforeAll
    static void init() throws Exception {
        Properties properties = new Properties();
        properties.put("url", "jdbc:hsqldb:mem:dbunittutor");
        properties.put("user", "sa");
        properties.put("password", "");

        dataSource = JDBCDataSourceFactory.createDataSource(properties);
        emf = Persistence.createEntityManagerFactory("dbunittutorPu");
    }

    @BeforeEach
    void setUp() throws Exception {
        IDatabaseConnection conn = new DatabaseDataSourceConnection(dataSource);
        IDataSet data = new XmlDataSet(EmployeeDaoTest.class.getResourceAsStream("/employees.xml"));
        DatabaseOperation.CLEAN_INSERT.execute(conn, data);

        assertEquals(3, conn.getRowCount("employee"));

        entityManager = emf.createEntityManager();
        employeeDao = new EmployeeDao(entityManager);
    }

    @Test
    void testListEmployees() {
        // When
        List<Employee> employees = employeeDao.listEmployees();

        // Then
        assertEquals(3, employees.size());
        assertEquals("Jack Doe", employees.get(0).getName());
    }

    @Test
    void testPersistEmployee() throws SQLException, DatabaseUnitException {
        // When
        Employee employee = new Employee();
        employee.setName("Jack Smith");
        employeeDao.persistEmployee(employee);

        // Then
        ITable tableDb = new SortedTable(DefaultColumnFilter
                .includedColumnsTable(new DatabaseDataSourceConnection(dataSource)
                        .createDataSet().getTable("employee"), new String[]{"name"}), new String[]{"name"});
        ITable tableXml = new SortedTable(DefaultColumnFilter
                .includedColumnsTable(new XmlDataSet(EmployeeDaoTest.class
                        .getResourceAsStream("/expectedEmployees.xml")).getTable("employee"),
                        new String[]{"name"}), new String[]{"name"});

        new DbUnitAssert().assertEquals(tableXml, tableDb);
    }

    @AfterEach
    void tearDown() {
        entityManager.close();
    }

    @AfterAll
    static void destroy() {
        emf.close();
    }
}
