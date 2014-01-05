package jtechlog.dbunittutor;

import java.util.List;
import javax.persistence.EntityManager;

public class EmployeeDaoJpa implements EmployeeDao {

    private EntityManager em;

    public void persistEmployee(Employee employee) {
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
    }

    public List<Employee> listEmployees(int firstResult, int maxResults) {
        return em.createQuery("select e from Employee e left join fetch e.phones", Employee.class)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }
}
