package lghdnov.msocial.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Echo", description = "Тестовые эхо-запросы")
@RestController
@RequestMapping("/api/echo")
public class EchoController {

    @Operation(summary = "Эхо GET-запрос")
    @ApiResponse(responseCode = "200", description = "Сообщение возвращено")
    @GetMapping
    public String echoGet(
        @Parameter(description = "Сообщение для возврата", example = "Hello")
        @RequestParam(defaultValue = "Hello") String message
    ) {
        return message;
    }

    @Operation(summary = "Эхо POST-запрос")
    @ApiResponse(responseCode = "200", description = "Сообщение возвращено")
    @PostMapping
    public String echoPost(
        @Parameter(description = "Сообщение для возврата", example = "Hello")
        @RequestBody String message
    ) {
        return message;
    }
}
