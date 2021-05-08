package com.bobocode.dao;

import com.bobocode.exception.CompanyDaoException;
import com.bobocode.model.Company;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class CompanyDaoImpl implements CompanyDao {
    private final EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.unwrap(Session.class).setDefaultReadOnly(true);
        try {

            Company company = entityManager
                    .createQuery("select c from Company c inner join fetch c.products where c.id = :id", Company.class)
                    .setParameter("id", id)
                    .getSingleResult();
            entityManager.getTransaction().commit();
            return company;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new CompanyDaoException("Error", e);
        } finally {
            entityManager.close();
        }
    }
}
