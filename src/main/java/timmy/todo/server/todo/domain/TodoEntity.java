package timmy.todo.server.todo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String body;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @Column(nullable = false)
    private Boolean completed;

    public TodoEntity(String title, String body, Boolean completed) {
        this.title = title;
        this.body = body;
        this.completed = completed != null && completed;
    }

    public void update(String title, String body, Boolean completed) {
        if (title != null) {
            this.title = title;
        }
        if (body != null) {
            this.body = body;
        }
        if (completed != null) {
            this.completed = completed;
        }
    }
}
