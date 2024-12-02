package whitewise.keycloakdemo.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtTokenUtils {

	public Map<String, Object> decodeToken(String authorizationHeader) {
		String token = authorizationHeader.replace("Bearer ", "").trim();
		DecodedJWT jwt = JWT.decode(token);
		return jwt.getClaims().entrySet().stream()
				  .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().as(Object.class)));
	}
}
