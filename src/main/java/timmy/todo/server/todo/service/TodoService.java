package timmy.todo.server.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import timmy.todo.server.todo.domain.TodoEntity;
import timmy.todo.server.todo.domain.TodoRepository;
import timmy.todo.server.todo.dto.TodoRequest;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    //    1	todo 리스트 목록에 아이템을 추가
    @Transactional
    public TodoEntity add(TodoRequest request) {
        TodoEntity todoEntity = new TodoEntity(request.title(), request.body(), request.completed());
        return this.todoRepository.save(todoEntity);
    }

    //    2	todo  리스트 목록 중 특정 아이템을 조회
    public TodoEntity searchById(Long id) {
        return this.todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    //    3	todo 리스트 전체 목록을 조회
    public List<TodoEntity> searchAll() {
        return this.todoRepository.findAll();
    }

    //    4	todo 리스트 목록 중 특정 아이템을 수정
    @Transactional
    public TodoEntity updateById(Long id, TodoRequest request) {
        TodoEntity todoEntity = this.searchById(id);
        todoEntity.update(request.title(), request.body(), request.completed());
        return todoEntity;
    }

    //    5	todo 리스트 목록 중 특정 아이템을 삭제
    @Transactional
    public void deleteById(Long id) {
        this.todoRepository.delete(this.searchById(id));
    }

}
