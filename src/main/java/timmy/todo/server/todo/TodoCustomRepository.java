package timmy.todo.server.todo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import timmy.todo.server.todo.dto.TodoQueryDto;

public interface TodoCustomRepository {

    /** 조건에 맞는 Todo를 페이지 단위로 반환한다. 조건이 비면 전체 행이 페이징 대상. */
    Page<Todo> findAll(Pageable pageable, TodoQueryDto queryDto);
}
