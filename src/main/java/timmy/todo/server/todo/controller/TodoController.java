package timmy.todo.server.todo.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import timmy.todo.server.todo.service.TodoService;
import timmy.todo.server.todo.domain.TodoEntity;
import timmy.todo.server.todo.dto.TodoRequest;
import timmy.todo.server.todo.dto.TodoResponse;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@AllArgsConstructor
@RestController
@RequestMapping("/todo")
public class TodoController {
    private final TodoService service;

    @PostMapping
    public ResponseEntity<TodoResponse> create(@RequestBody @Valid TodoRequest request) {
        TodoEntity result = this.service.add(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getId())
                .toUri();
        return ResponseEntity.created(location).body(TodoResponse.from(result));
    }

    @GetMapping("{id}")
    public ResponseEntity<TodoResponse> readOne(@PathVariable Long id) {
        TodoEntity result = this.service.searchById(id);
        return ResponseEntity.ok(TodoResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> readAll() {
        List<TodoEntity> list = this.service.searchAll();
        List<TodoResponse> response = list.stream().map(TodoResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 부분 수정이므로 @Valid 없음 (title만 빠진 요청도 허용)
    @PatchMapping("{id}")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id, @RequestBody TodoRequest request) {
        TodoEntity result = this.service.updateById(id, request);
        return ResponseEntity.ok(TodoResponse.from(result));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable Long id) {
        this.service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
