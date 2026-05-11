package lghdnov.msocial.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/echo")
public class EchoController {

    @GetMapping
    public String echoGet(@RequestParam(defaultValue = "Hello") String message) {
        return message;
    }

    @PostMapping
    public String echoPost(@RequestBody String message) {
        return message;
    }
}
