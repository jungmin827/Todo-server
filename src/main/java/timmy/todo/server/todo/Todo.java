package timmy.todo.server.todo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "todo")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", length = 2000)
    private String body;

    /**
     * {@code @Builder}는 필드 초기값을 무시한다. {@code @Builder.Default}가 없으면
     * 빌더로 만든 인스턴스의 completed가 null이 되어 NOT NULL 제약에 걸린다.
     */
    @Column(name = "completed", nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @CreatedDate
    @Column(name = "register_date", updatable = false)
    private LocalDateTime registerDate;

    @LastModifiedDate
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    /** 완료 처리 — 상태 변경 의도를 드러내는 도메인 메서드. */
    public void complete() {
        this.completed = Boolean.TRUE;
    }
}
