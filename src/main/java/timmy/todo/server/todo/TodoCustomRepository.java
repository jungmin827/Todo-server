package timmy.todo.server.todo;

import timmy.todo.server.todo.dto.TodoQueryDto;

import java.util.List;

public interface TodoCustomRepository {

    /** 조건에 맞는 Todo 전체를 반환한다 (페이징 없음). 조건이 비면 전체 행. */
    List<Todo> findAll(TodoQueryDto queryDto);
}
