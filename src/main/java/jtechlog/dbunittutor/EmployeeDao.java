package jtechlog.dbunittutor;

import java.util.List;
import javax.persistence.EntityManager;

public class EmployeeDao {

    private EntityManager em;

    public EmployeeDao(EntityManager em) {
        this.em = em;
    }

    public void persistEmployee(Employee employee) {
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
    }

    public List<Employee> listEmployees() {
        return em.createQuery("select distinct e from Employee e left join fetch e.phones order by e.name", Employee.class)
                .getResultList();
    }
}
