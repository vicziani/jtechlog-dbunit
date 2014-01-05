package jtechlog.dbunittutor;

import java.util.List;

public interface EmployeeDao {

    void persistEmployee(Employee employee);

    List<Employee> listEmployees(int firstResult, int maxResults);
}
