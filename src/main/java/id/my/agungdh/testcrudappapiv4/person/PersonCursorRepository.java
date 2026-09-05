package id.my.agungdh.testcrudappapiv4.person;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Keyset (cursor) pagination for infinite scroll. Ordering is
 * {@code <field> <direction>, id DESC} (id is the stable tiebreaker, newest
 * first). The soft-delete filter applies automatically via
 * {@code @SQLRestriction}, including to the cursor row lookup in the service.
 */
@Repository
public class PersonCursorRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * @param field  one of {@code id}, {@code name}, {@code birthDate}, {@code createdAt}
     * @param desc   sort direction of {@code field}
     * @param cursor last row of the previous page, or {@code null} for the first page
     * @param limit  max rows to fetch (caller passes {@code size + 1} to detect {@code hasNext})
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Person> findNext(String field, boolean desc, Person cursor, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Person> query = cb.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);

        List<Order> orders = new ArrayList<>();
        Path<Comparable> fieldPath = root.<Comparable>get(field);
        orders.add(desc ? cb.desc(fieldPath) : cb.asc(fieldPath));
        if (!"id".equals(field)) {
            orders.add(cb.desc(root.get("id")));
        }
        query.orderBy(orders);

        if (cursor != null) {
            query.where(keysetPredicate(cb, root, field, desc, cursor));
        }
        return em.createQuery(query).setMaxResults(limit).getResultList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate keysetPredicate(CriteriaBuilder cb, Root<Person> root, String field, boolean desc, Person cursor) {
        Path<Long> idPath = root.get("id");
        Long cursorId = cursor.getId();

        if ("id".equals(field)) {
            return desc ? cb.lessThan(idPath, cursorId) : cb.greaterThan(idPath, cursorId);
        }

        Path<Comparable> fieldPath = root.<Comparable>get(field);
        Comparable cursorValue = (Comparable) fieldPath.getJavaType().cast(readField(cursor, field));
        Predicate idTiebreak = cb.lessThan(idPath, cursorId);

        if (cursorValue == null) {
            // Postgres puts NULLS FIRST on DESC, LAST on ASC.
            return desc
                    ? cb.or(fieldPath.isNotNull(), cb.and(fieldPath.isNull(), idTiebreak))
                    : cb.and(fieldPath.isNull(), idTiebreak);
        }
        Predicate primary = desc
                ? cb.lessThan(fieldPath, cursorValue)
                : cb.greaterThan(fieldPath, cursorValue);
        return cb.or(primary, cb.and(cb.equal(fieldPath, cursorValue), idTiebreak));
    }

    private Object readField(Person cursor, String field) {
        return switch (field) {
            case "name" -> cursor.getName();
            case "birthDate" -> cursor.getBirthDate();
            case "createdAt" -> cursor.getCreatedAt();
            default -> throw new IllegalArgumentException("Invalid sort field: " + field);
        };
    }
}
