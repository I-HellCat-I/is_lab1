package com.hellcat.backend.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;

import com.hellcat.backend.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MovieRepository {

    @Inject
    private EntityManager em;

    public List<Movie> findAll() {
        return em.createQuery("SELECT m FROM Movie m JOIN FETCH m.screenwriter", Movie.class).getResultList();
    }

    @Transactional
    public Movie save(Movie movie) {
        if (movie.getId() == null) {
            em.persist(movie);
            return movie;
        } else {
            return em.merge(movie);
        }
    }

    public Optional<Movie> findById(Long id) {
        // Используем JOIN FETCH, чтобы сразу подгрузить связанные сущности
        // и избежать LazyInitializationException в дальнейшем
        List<Movie> result = em.createQuery(
                        "SELECT m FROM Movie m " +
                                "LEFT JOIN FETCH m.director " +
                                "LEFT JOIN FETCH m.screenwriter " +
                                "LEFT JOIN FETCH m.operator " +
                                "WHERE m.id = :id", Movie.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findFirst();
    }

    @Transactional
    public boolean deleteById(Long id) {
        Optional<Movie> movieOpt = findById(id);
        if (movieOpt.isPresent()) {
            em.remove(movieOpt.get());
            return true;
        }
        return false;
    }

    // --- МЕТОДЫ ДЛЯ СПЕЦОПЕРАЦИЙ ---

    @Transactional
    public void deleteByGoldenPalm(long palmCount) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("delete_movies_by_golden_palm");
        query.registerStoredProcedureParameter("palm_count", Long.class, ParameterMode.IN);
        query.setParameter("palm_count", palmCount);
        query.execute();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Object[]> groupMoviesByDirector() {
        return em.createNativeQuery("SELECT * FROM group_movies_by_director()").getResultList();
    }

    public Long countByGenre(String genre) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("count_movies_by_genre");
        query.registerStoredProcedureParameter("genre_name", String.class, ParameterMode.IN);
        // JPA не умеет мапить Enum напрямую в Enum PostgreSQL, поэтому передаем строкой
        query.setParameter("genre_name", genre);
        query.execute();
        return (Long) query.getSingleResult();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Object[]> getOperatorsWithoutOscars() {
        return em.createNativeQuery("SELECT * FROM get_operators_without_oscars()").getResultList();
    }

    @Transactional
    public void addOscarsToLongFilms(int minLength, long awardCount) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("add_oscars_to_long_films");
        query.registerStoredProcedureParameter("min_length", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("award_count", Long.class, ParameterMode.IN);
        query.setParameter("min_length", minLength);
        query.setParameter("award_count", awardCount);
        query.execute();
    }

    public long countByCriteria(String filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Movie> root = countQuery.from(Movie.class);

        countQuery.select(cb.count(root));

        if (filter != null && !filter.isBlank()) {
            Predicate predicate = buildFilterPredicate(cb, root, filter);
            countQuery.where(predicate);
        }

        return em.createQuery(countQuery).getSingleResult();
    }

    public List<Movie> findByCriteria(int page, int size, String[] sort, String filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Movie> criteriaQuery = cb.createQuery(Movie.class);
        Root<Movie> root = criteriaQuery.from(Movie.class);

        // --- 1. ФИЛЬТРАЦИЯ (WHERE) ---
        if (filter != null && !filter.isBlank()) {
            Predicate predicate = buildFilterPredicate(cb, root, filter);
            criteriaQuery.where(predicate);
        }

        // --- 2. СОРТИРОВКА (ORDER BY) ---
        if (sort != null && sort.length > 0) {
            List<Order> orders = new ArrayList<>();
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                String field = _sort[0];
                String direction = _sort.length > 1 ? _sort[1] : "asc";

                // Важно: проверяем поле, чтобы избежать SQL-инъекций в ORDER BY
                if (isValidSortField(field)) {
                    if ("desc".equalsIgnoreCase(direction)) {
                        orders.add(cb.desc(root.get(field)));
                    } else {
                        orders.add(cb.asc(root.get(field)));
                    }
                }
            }
            criteriaQuery.orderBy(orders);
        }

        // --- 3. ПАГИНАЦИЯ (LIMIT / OFFSET) ---
        TypedQuery<Movie> query = em.createQuery(criteriaQuery);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    private Predicate buildFilterPredicate(CriteriaBuilder cb, Root<Movie> root, String filter) {
        // Фильтруем по неполному совпадению в нескольких строковых полях
        String pattern = "%" + filter.toLowerCase() + "%";
        return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("director").get("name")), pattern),
                cb.like(cb.lower(root.get("screenwriter").get("name")), pattern),
                cb.like(cb.lower(root.get("operator").get("name")), pattern)
        );
    }

    private boolean isValidSortField(String field) {
        // Белый список полей, по которым можно сортировать. ЗАЩИТА!
        return List.of("id", "name", "oscarsCount", "budget", "totalBoxOffice", "length", "goldenPalmCount").contains(field);
    }
}