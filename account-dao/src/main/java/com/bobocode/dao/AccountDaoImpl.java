package com.bobocode.dao;

import com.bobocode.exception.AccountDaoException;
import com.bobocode.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        executeInsideEntityManager(em -> em.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return executeWithResult(em -> em.find(Account.class, id));
    }

    @Override
    public Account findByEmail(String email) {
         return executeWithResult(em ->
                 em.createQuery("select a from Account a where a.email=:email", Account.class)
                         .setParameter("email", email)
                         .getSingleResult());
    }

    @Override
    public List<Account> findAll() {
        return executeWithResult(em ->
                em.createQuery("select a from Account a", Account.class).getResultList());
    }

    @Override
    public void update(Account account) {
        executeInsideEntityManager(em -> em.merge(account));
    }

    @Override
    public void remove(Account account) {
        executeInsideEntityManager(em -> {
            Account managed = em.merge(account);
            em.remove(managed);
        });
    }

    private void executeInsideEntityManager(Consumer<EntityManager> emConsumer) {
        EntityManager entityManager = null;
        try {
            entityManager = emf.createEntityManager();
            entityManager.getTransaction().begin();
            emConsumer.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager != null) {
                entityManager.getTransaction().rollback();
            }
            throw new AccountDaoException("Cannot execute inside Entity Manager", e);
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private <T> T executeWithResult(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = null;
        T value = null;
        try {
            entityManager = emf.createEntityManager();
            entityManager.getTransaction().begin();
            value = entityManagerFunction.apply(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager != null) {
                entityManager.getTransaction().rollback();
            }
            throw new AccountDaoException("Cannot execute inside Entity Manager", e);
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return value;
    }
}

