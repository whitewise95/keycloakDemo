package whitewise.keycloakdemo.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		// "realm_access" 클레임에서 roles를 가져옵니다.
		Object rolesObject = jwt.getClaimAsMap("realm_access").get("roles");

		// rolesObject가 List일 경우만 처리
		if (rolesObject instanceof List<?>) {
			List<?> roles = (List<?>) rolesObject;
			return roles.stream()
						.filter(role -> role instanceof String)
						.map(role -> new SimpleGrantedAuthority((String) role))
						.collect(Collectors.toList());
		}

		// 빈 리스트 반환
		return List.of();
	}
}
