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
import org.junit.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import static org.junit.Assert.*;

public class EmployeeDaoTest {

    private static DataSource dataSource;
    private static EntityManagerFactory emf;

    private EntityManager entityManager;

    private EmployeeDao employeeDao;

    @BeforeClass
    public static void init() throws Exception {
        Properties properties = new Properties();
        properties.put("url", "jdbc:hsqldb:mem:dbunittutor");
        properties.put("user", "sa");
        properties.put("password", "");

        dataSource = JDBCDataSourceFactory.createDataSource(properties);
        emf = Persistence.createEntityManagerFactory("dbunittutorPu");
    }

    @Before
    public void setUp() throws Exception {
        IDatabaseConnection conn = new DatabaseDataSourceConnection(dataSource);
        IDataSet data = new XmlDataSet(EmployeeDaoTest.class.getResourceAsStream("/employees.xml"));
        DatabaseOperation.CLEAN_INSERT.execute(conn, data);

        assertEquals(3, conn.getRowCount("employee"));

        entityManager = emf.createEntityManager();
        employeeDao = new EmployeeDaoJpa();
        ((EmployeeDaoJpa) employeeDao).setEm(entityManager);
    }

    @Test
    public void testPersistEmployee() throws SQLException, DatabaseUnitException {
        // When
        Employee employee = new Employee();
        employee.setName("name4");
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

    @Test
    public void testListEmployees() {
        // When
        List<Employee> employees = employeeDao.listEmployees(1, 2);

        // Then
        assertEquals(2, employees.size());
        assertTrue("A name prefixszel kell kezdodnie", employees.get(0).getName().startsWith("name"));
    }

    @After
    public void tearDown() {
        entityManager.close();
    }

    @AfterClass
    public static void destroy() {
        emf.close();
    }
}
