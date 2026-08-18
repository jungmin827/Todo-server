package timmy.todo.server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Timmy Todo API",
                version = "1",
                description = "Todo 관리 API. 모든 엔드포인트는 요청 헤더에 X-API-VERSION=1 이 필요하다."
        ),
        servers = @Server(url = "/", description = "Default Server URL")
)
public class OpenApiConfig {
}
