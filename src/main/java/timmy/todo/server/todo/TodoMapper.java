package timmy.todo.server.todo;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import timmy.todo.server.todo.dto.TodoInsertDto;
import timmy.todo.server.todo.dto.TodoResponseDto;
import timmy.todo.server.todo.dto.TodoUpdateDto;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TodoMapper {

    TodoResponseDto toResponseDto(Todo entity);

    List<TodoResponseDto> toResponseDtoList(List<Todo> entities);

    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "registerDate", ignore = true)
    @Mapping(target = "modifyDate", ignore = true)
    @Mapping(target = "completed", source = "completed", defaultValue = "false")
    Todo toEntity(TodoInsertDto dto);

    /** 부분 수정 — null로 온 필드는 기존 값을 유지한다. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idx", ignore = true)
    @Mapping(target = "registerDate", ignore = true)
    @Mapping(target = "modifyDate", ignore = true)
    void updateEntity(TodoUpdateDto dto, @MappingTarget Todo entity);
}
