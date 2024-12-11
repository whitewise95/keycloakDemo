package whitewise.keycloakdemo.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
public class TestController {

	@GetMapping("/code")
	public ResponseEntity<String> handleAuthCode(@RequestParam("code") String code) {
		return ResponseEntity.ok(code);
	}

	@GetMapping("/pkce")
	public Map<String, String> pkce() throws NoSuchAlgorithmException {
		String verifier = generateVerifier();
		String CodeChallenge = generateCodeChallenge(verifier);

		Map<String, String> map = new HashMap<>();
		map.put("verifier", verifier);
		map.put("codeChallenge", CodeChallenge);
		return map;
	}

	private String generateVerifier() {
		int length = 43 + new Random().nextInt(10); // Length: 43 to 53 (RFC 7636)
		StringBuilder verifier = new StringBuilder();
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			verifier.append(chars.charAt(random.nextInt(chars.length())));
		}
		return verifier.toString();
	}

	private String generateCodeChallenge(String verifier) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
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
