package timmy.todo.server.todo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;
import timmy.todo.server.TestcontainersConfiguration;
import timmy.todo.server.todo.domain.TodoEntity;
import timmy.todo.server.todo.domain.TodoRepository;
import timmy.todo.server.todo.dto.TodoRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TodoServiceTest {

    @Autowired
    TodoService todoService;

    @Autowired
    TodoRepository todoRepository;

    @BeforeEach
    void clean() {
        todoRepository.deleteAll();
    }

    @Test
    @DisplayName("생성 시 completed 기본값은 false이고 생성/수정 시각이 채워진다")
    void add() {
        TodoEntity saved = todoService.add(new TodoRequest("장보기", "우유", null));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCompleted()).isFalse();          // 요청에 없어도 NOT NULL 보장
        assertThat(saved.getCreatedDate()).isNotNull();      // JPA Auditing 동작 확인
        assertThat(saved.getModifiedDate()).isNotNull();
    }

    @Test
    @DisplayName("부분 수정 시 null로 넘어온 필드는 기존 값을 유지한다")
    void updateById() {
        TodoEntity saved = todoService.add(new TodoRequest("장보기", "우유", null));

        todoService.updateById(saved.getId(), new TodoRequest(null, null, true));

        TodoEntity found = todoRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCompleted()).isTrue();
        assertThat(found.getTitle()).isEqualTo("장보기");      // save() 없이 변경 감지로 반영
        assertThat(found.getBody()).isEqualTo("우유");
    }

    @Test
    @DisplayName("없는 id를 조회하거나 삭제하면 404")
    void notFound() {
        assertThatThrownBy(() -> todoService.searchById(9999L))
                .isInstanceOf(ResponseStatusException.class);

        assertThatThrownBy(() -> todoService.deleteById(9999L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
