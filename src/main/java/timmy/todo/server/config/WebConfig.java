package timmy.todo.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

/**
 * 페이지 응답 직렬화 설정.
 *
 * <p>{@code PageSerializationMode.VIA_DTO}를 켜면 {@code Page} 응답이 {@code PageImpl}
 * 전체 게터가 아니라 {@code PagedModel} 형태로 나간다.
 * <pre>{@code {"content":[...], "page":{"size","number","totalElements","totalPages"}}}</pre>
 * Controller·Service 시그니처는 {@code Page<XxxResponseDto>} 그대로라 표준은 유지된다.
 * Spring이 띄우던 "PageImpl 직렬화는 구조 안정성이 보장되지 않는다" 경고도 사라진다.
 *
 * <p><b>주의</b>: {@code @EnableSpringDataWebSupport}를 명시하면 Boot의
 * {@code SpringDataWebAutoConfiguration}이
 * {@code @ConditionalOnMissingBean(PageableHandlerMethodArgumentResolver.class)}로 물러난다.
 * 그러면 {@code spring.data.web.pageable.*} 프로퍼티가 더 이상 적용되지 않으므로,
 * 기본/최대 페이지 크기를 아래 커스터마이저에서 명시적으로 잡아준다.
 */
@Configuration
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {

    /** 파라미터 없이 호출했을 때의 페이지 크기. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** 한 번에 가져갈 수 있는 최대 건수. 초과 요청은 이 값으로 잘린다. */
    private static final int MAX_PAGE_SIZE = 2000;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setFallbackPageable(PageRequest.of(0, DEFAULT_PAGE_SIZE));
            resolver.setMaxPageSize(MAX_PAGE_SIZE);
        };
    }
}
