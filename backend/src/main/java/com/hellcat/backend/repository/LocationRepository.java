package com.hellcat.backend.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import com.hellcat.backend.model.Location;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class LocationRepository {

    @Inject
    private EntityManager em;

    public Optional<Location> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Location location = em.find(Location.class, id);
        return Optional.ofNullable(location);
    }

    public List<Location> findAll() {
        return em.createQuery("SELECT l FROM locations l", Location.class).getResultList();
    }

    public Location save(Location location) {
        if (location.getId() == null) {
            em.persist(location);
        } else {
            location = em.merge(location);
        }
        return location;
    }
}
