package com.bobocode.dao;

import com.bobocode.dao.exception.DaoException;
import com.bobocode.model.Photo;
import com.bobocode.model.PhotoComment;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private final EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        performWithinTx(em -> em.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return readWithinTx(em -> em.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        return readWithinTx(em -> em.createQuery("select p from Photo p", Photo.class).getResultList());
    }

    @Override
    public void remove(Photo photo) {
        performWithinTx(em -> {
            Photo managed = em.merge(photo);
            em.remove(managed);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        performWithinTx(em -> {
            Photo photo = em.find(Photo.class, photoId);
            photo.addComment(PhotoComment.builder().photo(photo).text(comment).build());
        });
    }

    private void performWithinTx(Consumer<EntityManager> entityManagerConsumer) {
        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.unwrap(Session.class).setDefaultReadOnly(true);
            entityManager.getTransaction().begin();
            entityManagerConsumer.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager != null) {
                entityManager.getTransaction().rollback();
            }
            throw new DaoException("Error while perform dao operation", e);
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    private <T> T readWithinTx(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            entityManager.unwrap(Session.class).setDefaultReadOnly(true);
            entityManager.getTransaction().begin();
            T value = entityManagerFunction.apply(entityManager);
            entityManager.getTransaction().commit();
            return value;
        } catch (Exception e) {
            if (entityManager != null) {
                entityManager.getTransaction().rollback();
            }
            throw new DaoException("Error while perform dao operation", e);
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }
}
