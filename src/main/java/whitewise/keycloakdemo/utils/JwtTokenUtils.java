package whitewise.keycloakdemo.utils;

import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class JwtTokenUtils {

	public Map<String, Object> decodeToken(String authorizationHeader) {
		String pureToken = authorizationHeader.replace("Bearer ", "").trim();
		NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri("http://localhost:8090/realms/myrealm/protocol/openid-connect/certs")
															.build();

		Jwt decode = nimbusJwtDecoder.decode(pureToken);
		decode.getClaims().forEach((key, value) -> System.out.println(key + ": " + value));
		return decode.getClaims();
	}
}
