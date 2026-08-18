package timmy.todo.server.todo;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import timmy.todo.server.common.BaseCustomRepository;
import timmy.todo.server.todo.dto.TodoQueryDto;

import java.util.List;

import static timmy.todo.server.todo.QTodo.todo;

@Repository
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl extends BaseCustomRepository implements TodoCustomRepository {

    @Override
    public List<Todo> findAll(TodoQueryDto queryDto) {
        return queryFactory
                .selectFrom(todo)
                .where(buildSearchCondition(queryDto))
                .orderBy(todo.idx.desc())
                .fetch();
    }

    private BooleanBuilder buildSearchCondition(TodoQueryDto queryDto) {
        BooleanBuilder builder = new BooleanBuilder();

        if (queryDto == null) {
            return builder;
        }

        if (StringUtils.hasText(queryDto.getTitle())) {
            builder.and(todo.title.containsIgnoreCase(queryDto.getTitle()));
        }
        if (StringUtils.hasText(queryDto.getBody())) {
            builder.and(todo.body.containsIgnoreCase(queryDto.getBody()));
        }
        if (queryDto.getCompleted() != null) {
            builder.and(todo.completed.eq(queryDto.getCompleted()));
        }
        if (queryDto.getRegisterDateFrom() != null) {
            builder.and(todo.registerDate.goe(queryDto.getRegisterDateFrom()));
        }
        if (queryDto.getRegisterDateTo() != null) {
            builder.and(todo.registerDate.loe(queryDto.getRegisterDateTo()));
        }
        if (queryDto.getModifyDateFrom() != null) {
            builder.and(todo.modifyDate.goe(queryDto.getModifyDateFrom()));
        }
        if (queryDto.getModifyDateTo() != null) {
            builder.and(todo.modifyDate.loe(queryDto.getModifyDateTo()));
        }

        return builder;
    }
}
