package timmy.todo.server.todo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import timmy.todo.server.common.ResponseData;
import timmy.todo.server.common.ResponseDataType;
import timmy.todo.server.exception.NotFoundException;
import timmy.todo.server.todo.dto.TodoInsertDto;
import timmy.todo.server.todo.dto.TodoQueryDto;
import timmy.todo.server.todo.dto.TodoResponseDto;
import timmy.todo.server.todo.dto.TodoUpdateDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    // ========== CREATE (생성) ==========

    public TodoResponseDto insertTodo(TodoInsertDto dto) {
        Todo entity = todoMapper.toEntity(dto);
        Todo saved = todoRepository.save(entity);
        return todoMapper.toResponseDto(saved);
    }

    // ========== READ (조회) ==========

    @Transactional(readOnly = true)
    public TodoResponseDto getTodoByIdx(Long idx) {
        Todo entity = todoRepository.findById(idx)
                .orElseThrow(() -> new NotFoundException("Todo를 찾을 수 없습니다."));
        return todoMapper.toResponseDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<TodoResponseDto> getTodoList(Pageable pageable, TodoQueryDto queryDto) {
        return todoRepository.findAll(pageable, queryDto)
                .map(todoMapper::toResponseDto);
    }

    // ========== UPDATE (수정) ==========

    public TodoResponseDto updateTodoByIdx(Long idx, TodoUpdateDto dto) {
        Todo entity = todoRepository.findById(idx)
                .orElseThrow(() -> new NotFoundException("Todo를 찾을 수 없습니다."));

        todoMapper.updateEntity(dto, entity);

        // save()만 하면 @LastModifiedDate가 커밋 시점에 찍혀 응답에 갱신 전 modifyDate가 실린다.
        // 매핑 전에 flush해서 응답과 DB를 일치시킨다.
        Todo updated = todoRepository.saveAndFlush(entity);
        return todoMapper.toResponseDto(updated);
    }

    // ========== DELETE (삭제) ==========

    public ResponseData<Void> deleteTodoByIdx(Long idx) {
        Todo entity = todoRepository.findById(idx)
                .orElseThrow(() -> new NotFoundException("Todo를 찾을 수 없습니다."));

        todoRepository.delete(entity);
        return ResponseData.<Void>builder()
                .result(ResponseDataType.TRUE)
                .build();
    }
}
