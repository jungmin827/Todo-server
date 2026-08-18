package timmy.todo.server.todo;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import timmy.todo.server.common.BaseCustomRepository;
import timmy.todo.server.exception.BadValidationException;
import timmy.todo.server.todo.dto.TodoQueryDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static timmy.todo.server.todo.QTodo.todo;

@Repository
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl extends BaseCustomRepository implements TodoCustomRepository {

    /**
     * 정렬 허용 필드. 여기 없는 필드로 정렬 요청이 오면 조용히 무시하지 않고 400으로 거절한다.
     * (무시하면 클라이언트는 정렬이 먹은 줄 알고, 서버는 다른 순서를 주는 상태가 된다.)
     */
    private static final Map<String, ComparableExpressionBase<?>> SORTABLE_PATHS = new LinkedHashMap<>();

    static {
        SORTABLE_PATHS.put("idx", todo.idx);
        SORTABLE_PATHS.put("title", todo.title);
        SORTABLE_PATHS.put("completed", todo.completed);
        SORTABLE_PATHS.put("registerDate", todo.registerDate);
        SORTABLE_PATHS.put("modifyDate", todo.modifyDate);
    }

    @Override
    public Page<Todo> findAll(Pageable pageable, TodoQueryDto queryDto) {
        BooleanBuilder builder = buildSearchCondition(queryDto);

        List<Todo> content = queryFactory
                .selectFrom(todo)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * {@code ?sort=title,asc} 같은 요청을 QueryDSL 정렬로 변환한다.
     * 정렬 조건이 없으면 idx 내림차순(최신순)이 기본이다.
     */
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier<?>[]{todo.idx.desc()};
        }

        return sort.stream()
                .map(order -> {
                    ComparableExpressionBase<?> path = SORTABLE_PATHS.get(order.getProperty());
                    if (path == null) {
                        throw new BadValidationException("정렬할 수 없는 필드입니다: " + order.getProperty()
                                + " (가능: " + String.join(", ", SORTABLE_PATHS.keySet()) + ")");
                    }
                    return order.isAscending() ? path.asc() : path.desc();
                })
                .toArray(OrderSpecifier<?>[]::new);
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
