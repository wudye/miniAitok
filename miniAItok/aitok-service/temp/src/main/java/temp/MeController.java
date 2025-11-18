package temp;



import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/me")
    public String me(@AuthenticationPrincipal Jwt jwt) {
        // 从 JWT claim 读取 userId（或你在网关里放到 claim 的字段）
        String userId = jwt.getClaimAsString("userId");
        return "userId=" + userId;
    }
}
