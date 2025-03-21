package graduation.spokera.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProtectedController {

    @GetMapping("/protected")
    public Map<String, String> protectedEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ JWT 인증 성공!");
        return response;
    }
}
