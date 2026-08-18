package timmy.todo.server.common;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * QueryDSL Custom Repository 베이스. 구현체는 이 클래스를 상속해 {@code queryFactory}를 그대로 쓴다.
 */
public abstract class BaseCustomRepository {

    @Autowired
    protected JPAQueryFactory queryFactory;
}
