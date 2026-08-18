package timmy.todo.server.todo.dto;

import jakarta.validation.constraints.NotBlank;

public record TodoRequest(
        @NotBlank String title,
        String body,
        Boolean completed
) {
}
