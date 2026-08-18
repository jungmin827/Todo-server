package timmy.todo.server.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Controller 진입/종료 자동 로깅 (AOP).
 *
 * <p>개별 메서드에서 {@code log.info}/{@code log.error}를 호출하지 않는다 — 정상 흐름은 이 클래스가,
 * 예외 흐름은 {@code ExceptionController}가 책임진다.
 *
 * <p>이 프로젝트에는 인증 계층이 없어 레퍼런스 구현의 사용자 클레임(idx/user_name) 로깅은 생략했다.
 */
@Aspect
@Component
@Slf4j
public class AspectLogger {

    private static final String PACKAGE_NAME = "timmy.todo.server.";
    private static final int SIGNATURE_STRING_LENGTH = -45;

    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Before("execution(* " + PACKAGE_NAME + "*.*Controller.*(..))")
    public void logControllerEnter(JoinPoint joinPoint) {
        START_TIME.set(System.currentTimeMillis());
        log.debug("INPUT<!;>[{}] args: {}", signature(joinPoint), parameters(joinPoint));
    }

    @AfterReturning(pointcut = "execution(* " + PACKAGE_NAME + "*.*Controller.*(..))", returning = "output")
    public void logControllerReturns(JoinPoint joinPoint, ResponseEntity<?> output) {
        logOutputData(joinPoint, output);
    }

    @Before("execution(* " + PACKAGE_NAME + "*.*.*Controller.*(..))")
    public void logControllerEnter2Depth(JoinPoint joinPoint) {
        START_TIME.set(System.currentTimeMillis());
        log.debug("INPUT<!;>[{}] args: {}", signature(joinPoint), parameters(joinPoint));
    }

    @AfterReturning(pointcut = "execution(* " + PACKAGE_NAME + "*.*.*Controller.*(..))", returning = "output")
    public void logControllerReturns2Depth(JoinPoint joinPoint, ResponseEntity<?> output) {
        logOutputData(joinPoint, output);
    }

    private void logOutputData(JoinPoint joinPoint, ResponseEntity<?> output) {
        Long startedAt = START_TIME.get();
        START_TIME.remove();
        long executionTime = startedAt == null ? -1L : System.currentTimeMillis() - startedAt;

        if (output.getStatusCode().value() >= 400) {
            log.error("{}<!;>{}<!;>{}<!;>{}", signature(joinPoint), output.getStatusCode(),
                    parameters(joinPoint), output.getBody());
            return;
        }

        log.info("{}<!;>{}<!;>{}ms", signature(joinPoint), output.getStatusCode(), executionTime);
        log.debug("OUTPUT<!;>{}<!;>{}<!;>{}", signature(joinPoint), parameters(joinPoint), output.getBody());
    }

    private String signature(JoinPoint joinPoint) {
        return String.format("%" + SIGNATURE_STRING_LENGTH + "s",
                joinPoint.getSignature().toShortString()).trim();
    }

    private String parameters(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return "No have arguments.";
        }
        return Arrays.stream(args)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }
}
