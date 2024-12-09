package whitewise.keycloakdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import whitewise.keycloakdemo.entity.UserRepository;
import whitewise.keycloakdemo.keycloak.CustomUserLookupProviderFactory;

@Configuration
public class KeycloakProviderConfig {

	@Bean
	public CustomUserLookupProviderFactory jpaUserStorageProviderFactory(UserRepository userRepository) {
		return new CustomUserLookupProviderFactory(userRepository);
	}
}
