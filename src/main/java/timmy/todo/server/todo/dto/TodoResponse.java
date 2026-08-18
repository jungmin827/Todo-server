package timmy.todo.server.todo.dto;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import timmy.todo.server.todo.domain.TodoEntity;

import java.time.LocalDateTime;

public record TodoResponse(
        Long id,
        String title,
        String body,
        Boolean completed,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate,
        String url
) {

    public static TodoResponse from(TodoEntity todo) {
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/todo/")
                .path(String.valueOf(todo.getId()))
                .toUriString();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getBody(),
                todo.getCompleted(),
                todo.getCreatedDate(),
                todo.getModifiedDate(),
                url
        );
    }
}
