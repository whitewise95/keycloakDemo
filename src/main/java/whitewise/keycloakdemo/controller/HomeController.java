package whitewise.keycloakdemo.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import whitewise.keycloakdemo.utils.JwtTokenUtils;

@RestController
@RequiredArgsConstructor
public class HomeController {

	@GetMapping("/code")
	public ResponseEntity<String> handleAuthCode(@RequestParam("code") String code) {
		return ResponseEntity.ok(code);
	}

	@PostMapping("/ciba-request")
	public ResponseEntity<Void> cibaRequest(@RequestHeader Map<String, String> headers, @RequestBody Map<String, String> body) {
		System.out.println("#" + headers);
		System.out.println("#" + body);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping("/")
	public Map<String, Object> index(@RequestHeader("Authorization") String authorization) {
		JwtTokenUtils jwtTokenUtils = new JwtTokenUtils();
		return jwtTokenUtils.decodeToken(authorization);
	}
}
