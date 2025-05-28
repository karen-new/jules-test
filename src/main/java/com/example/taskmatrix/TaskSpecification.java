package com.example.taskmatrix;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> getTasksByCriteria(
            String label,
            LocalDate dueDateBefore,
            LocalDate dueDateAfter,
            Importance importance,
            Urgency urgency,
            Quadrant quadrant) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (label != null && !label.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("label")), "%" + label.toLowerCase() + "%"));
            }

            if (dueDateBefore != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), dueDateBefore));
            }

            if (dueDateAfter != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), dueDateAfter));
            }

            if (quadrant != null) {
                // If quadrant is specified, derive importance and urgency from it
                switch (quadrant) {
                    case IMPORTANT_URGENT:
                        predicates.add(criteriaBuilder.equal(root.get("importance"), Importance.IMPORTANT));
                        predicates.add(criteriaBuilder.equal(root.get("urgency"), Urgency.URGENT));
                        break;
                    case IMPORTANT_NOT_URGENT:
                        predicates.add(criteriaBuilder.equal(root.get("importance"), Importance.IMPORTANT));
                        predicates.add(criteriaBuilder.equal(root.get("urgency"), Urgency.NOT_URGENT));
                        break;
                    case NOT_IMPORTANT_URGENT:
                        predicates.add(criteriaBuilder.equal(root.get("importance"), Importance.NOT_IMPORTANT));
                        predicates.add(criteriaBuilder.equal(root.get("urgency"), Urgency.URGENT));
                        break;
                    case NOT_IMPORTANT_NOT_URGENT:
                        predicates.add(criteriaBuilder.equal(root.get("importance"), Importance.NOT_IMPORTANT));
                        predicates.add(criteriaBuilder.equal(root.get("urgency"), Urgency.NOT_URGENT));
                        break;
                }
            } else {
                // If quadrant is not specified, use individual importance and urgency filters
                if (importance != null) {
                    predicates.add(criteriaBuilder.equal(root.get("importance"), importance));
                }
                if (urgency != null) {
                    predicates.add(criteriaBuilder.equal(root.get("urgency"), urgency));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
