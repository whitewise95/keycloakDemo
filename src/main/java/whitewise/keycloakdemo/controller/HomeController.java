package whitewise.keycloakdemo.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

	@GetMapping("/")
	public String index(@AuthenticationPrincipal Jwt jwt) {
		return String.format("Hello, %s!", jwt.getClaimAsString("preferred_username"));
	}

	@GetMapping("/protected/premium")
	@Secured("ROLE_USER")
	public String premium(@AuthenticationPrincipal Jwt jwt) {
		return String.format("Hello, %s!", jwt.getClaimAsString("preferred_username"));
	}
}
