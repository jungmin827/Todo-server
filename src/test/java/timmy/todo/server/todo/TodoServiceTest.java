package timmy.todo.server.todo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import timmy.todo.server.common.ResponseData;
import timmy.todo.server.common.ResponseDataType;
import timmy.todo.server.exception.BadValidationException;
import timmy.todo.server.exception.NotFoundException;
import timmy.todo.server.todo.dto.TodoInsertDto;
import timmy.todo.server.todo.dto.TodoQueryDto;
import timmy.todo.server.todo.dto.TodoResponseDto;
import timmy.todo.server.todo.dto.TodoUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
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
    @DisplayName("생성 시 completed 기본값은 false이고 등록/수정 시각이 채워진다")
    void insertTodo() {
        TodoResponseDto saved = todoService.insertTodo(
                TodoInsertDto.builder().title("장보기").body("우유").build());

        assertThat(saved.getIdx()).isNotNull();
        assertThat(saved.getCompleted()).isFalse();          // 요청에 없어도 NOT NULL 보장
        assertThat(saved.getRegisterDate()).isNotNull();     // JPA Auditing 동작 확인
        assertThat(saved.getModifyDate()).isNotNull();
    }

    @Test
    @DisplayName("MapStruct 매퍼가 빈 껍데기가 아니라 실제로 필드를 채운다")
    void mapperIsNotHollow() {
        TodoResponseDto saved = todoService.insertTodo(
                TodoInsertDto.builder().title("장보기").body("우유").completed(true).build());

        assertThat(saved.getTitle()).isEqualTo("장보기");
        assertThat(saved.getBody()).isEqualTo("우유");
        assertThat(saved.getCompleted()).isTrue();
    }

    @Test
    @DisplayName("부분 수정 시 null로 넘어온 필드는 기존 값을 유지한다")
    void updateTodoByIdx() {
        TodoResponseDto saved = todoService.insertTodo(
                TodoInsertDto.builder().title("장보기").body("우유").build());

        TodoResponseDto updated = todoService.updateTodoByIdx(
                saved.getIdx(), TodoUpdateDto.builder().completed(true).build());

        assertThat(updated.getCompleted()).isTrue();
        assertThat(updated.getTitle()).isEqualTo("장보기");
        assertThat(updated.getBody()).isEqualTo("우유");
    }

    @Test
    @DisplayName("수정 응답의 modifyDate가 갱신 전 값이 아니라 갱신된 값이다")
    void updateResponseCarriesFreshModifyDate() {
        TodoResponseDto saved = todoService.insertTodo(
                TodoInsertDto.builder().title("장보기").build());

        TodoResponseDto updated = todoService.updateTodoByIdx(
                saved.getIdx(), TodoUpdateDto.builder().title("장보기 수정").build());

        assertThat(updated.getModifyDate()).isAfter(saved.getModifyDate());
    }

    @Test
    @DisplayName("삭제는 ResponseData<Void> result=TRUE를 반환한다")
    void deleteTodoByIdx() {
        TodoResponseDto saved = todoService.insertTodo(
                TodoInsertDto.builder().title("장보기").build());

        ResponseData<Void> response = todoService.deleteTodoByIdx(saved.getIdx());

        assertThat(response.getResult()).isEqualTo(ResponseDataType.TRUE);
        assertThat(todoRepository.findById(saved.getIdx())).isEmpty();
    }

    @Test
    @DisplayName("검색 조건이 없으면 전체가 페이징 대상이 된다")
    void getTodoListReturnsAll() {
        todoService.insertTodo(TodoInsertDto.builder().title("장보기").completed(true).build());
        todoService.insertTodo(TodoInsertDto.builder().title("설거지").build());
        todoService.insertTodo(TodoInsertDto.builder().title("빨래").build());

        Page<TodoResponseDto> page = todoService.getTodoList(
                PageRequest.of(0, 20), TodoQueryDto.builder().build());

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(TodoResponseDto::getTitle)
                .containsExactly("빨래", "설거지", "장보기");   // idx 내림차순
    }

    @Test
    @DisplayName("size보다 많으면 페이지가 나뉘고 totalElements는 전체 건수를 센다")
    void getTodoListPaginates() {
        for (int i = 1; i <= 5; i++) {
            todoService.insertTodo(TodoInsertDto.builder().title("할일" + i).build());
        }

        Page<TodoResponseDto> first = todoService.getTodoList(
                PageRequest.of(0, 2), TodoQueryDto.builder().build());
        Page<TodoResponseDto> last = todoService.getTodoList(
                PageRequest.of(2, 2), TodoQueryDto.builder().build());

        assertThat(first.getTotalElements()).isEqualTo(5);
        assertThat(first.getTotalPages()).isEqualTo(3);
        assertThat(first.getNumberOfElements()).isEqualTo(2);
        assertThat(first.isFirst()).isTrue();
        assertThat(first.isLast()).isFalse();

        assertThat(last.getNumberOfElements()).isEqualTo(1);   // 5건 중 마지막 1건
        assertThat(last.isLast()).isTrue();
    }

    @Test
    @DisplayName("queryDto가 null이어도 페이징 조회가 동작한다")
    void getTodoListWithNullQuery() {
        todoService.insertTodo(TodoInsertDto.builder().title("장보기").build());
        todoService.insertTodo(TodoInsertDto.builder().title("설거지").build());

        assertThat(todoService.getTodoList(PageRequest.of(0, 20), null).getTotalElements())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("sort 파라미터가 실제 정렬에 반영된다")
    void getTodoListHonorsSort() {
        todoService.insertTodo(TodoInsertDto.builder().title("다").build());
        todoService.insertTodo(TodoInsertDto.builder().title("가").build());
        todoService.insertTodo(TodoInsertDto.builder().title("나").build());

        Page<TodoResponseDto> asc = todoService.getTodoList(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title")),
                TodoQueryDto.builder().build());

        assertThat(asc.getContent()).extracting(TodoResponseDto::getTitle)
                .containsExactly("가", "나", "다");
    }

    @Test
    @DisplayName("정렬 조건이 없으면 idx 내림차순이 기본이다")
    void getTodoListDefaultSort() {
        todoService.insertTodo(TodoInsertDto.builder().title("먼저").build());
        todoService.insertTodo(TodoInsertDto.builder().title("나중").build());

        Page<TodoResponseDto> page = todoService.getTodoList(
                PageRequest.of(0, 20), TodoQueryDto.builder().build());

        assertThat(page.getContent()).extracting(TodoResponseDto::getTitle)
                .containsExactly("나중", "먼저");
    }

    @Test
    @DisplayName("허용하지 않은 필드로 정렬하면 조용히 무시하지 않고 BadValidationException")
    void getTodoListRejectsUnknownSortField() {
        todoService.insertTodo(TodoInsertDto.builder().title("장보기").build());

        assertThatThrownBy(() -> todoService.getTodoList(
                PageRequest.of(0, 20, Sort.by("password")), TodoQueryDto.builder().build()))
                .isInstanceOf(BadValidationException.class)
                .hasMessageContaining("password");
    }

    @Test
    @DisplayName("검색 조건은 페이징 전에 적용되어 totalElements도 걸러진 건수다")
    void getTodoList() {
        todoService.insertTodo(TodoInsertDto.builder().title("장보기").completed(true).build());
        todoService.insertTodo(TodoInsertDto.builder().title("설거지").build());

        Page<TodoResponseDto> completedOnly = todoService.getTodoList(
                PageRequest.of(0, 20), TodoQueryDto.builder().completed(true).build());

        assertThat(completedOnly.getTotalElements()).isEqualTo(1);
        assertThat(completedOnly.getContent().get(0).getTitle()).isEqualTo("장보기");
    }

    @Test
    @DisplayName("없는 idx를 조회/수정/삭제하면 NotFoundException")
    void notFound() {
        assertThatThrownBy(() -> todoService.getTodoByIdx(9999L))
                .isInstanceOf(NotFoundException.class);

        assertThatThrownBy(() -> todoService.updateTodoByIdx(9999L, TodoUpdateDto.builder().build()))
                .isInstanceOf(NotFoundException.class);

        assertThatThrownBy(() -> todoService.deleteTodoByIdx(9999L))
                .isInstanceOf(NotFoundException.class);
    }
}
