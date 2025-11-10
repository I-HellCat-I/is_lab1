package com.hellcat.backend.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import com.hellcat.backend.model.Person;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonRepository {

    @Inject
    private EntityManager em;

    public Optional<Person> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        Person person = em.find(Person.class, id);
        return Optional.ofNullable(person);
    }

    /**
     * Возвращает список всех зарегистрированных в системе персоналий.
     * Используем JOIN FETCH для немедленной загрузки связанных локаций,
     * чтобы избежать проблем с ленивой загрузкой при сериализации.
     * @return Список всех объектов Person.
     */
    public List<Person> findAll() {
        return em.createQuery(
                        "SELECT p FROM Person p JOIN FETCH p.location", Person.class)
                .getResultList();
    }
}