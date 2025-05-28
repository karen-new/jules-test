package com.example.taskmatrix;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.beans.Transient;
import java.time.LocalDate;

@Entity
@Data // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Size(max = 100, message = "Label cannot exceed 100 characters")
    private String label;

    private LocalDate dueDate;

    @NotNull(message = "Importance cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Importance importance;

    @NotNull(message = "Urgency cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgency urgency;

    /**
     * Calculates and returns the quadrant of the task based on its importance and urgency.
     * This method is transient and not persisted in the database.
     *
     * @return The {@link Quadrant} of the task.
     */
    @Transient
    public Quadrant getQuadrant() {
        if (importance == Importance.IMPORTANT && urgency == Urgency.URGENT) {
            return Quadrant.IMPORTANT_URGENT;
        } else if (importance == Importance.IMPORTANT && urgency == Urgency.NOT_URGENT) {
            return Quadrant.IMPORTANT_NOT_URGENT;
        } else if (importance == Importance.NOT_IMPORTANT && urgency == Urgency.URGENT) {
            return Quadrant.NOT_IMPORTANT_URGENT;
        } else if (importance == Importance.NOT_IMPORTANT && urgency == Urgency.NOT_URGENT) {
            return Quadrant.NOT_IMPORTANT_NOT_URGENT;
        }
        return null; // Should not happen if importance and urgency are always set
    }
}
