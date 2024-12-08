package whitewise.keycloakdemo.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;
import whitewise.keycloakdemo.entity.UserRepository;
import whitewise.keycloakdemo.provider.JdbcUserStorageProvider;

public class CustomUserLookupProviderFactory implements UserStorageProviderFactory<JdbcUserStorageProvider> {

	private final UserRepository userRepository;

	public CustomUserLookupProviderFactory(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public JdbcUserStorageProvider create(KeycloakSession session, ComponentModel model) {
		return new JdbcUserStorageProvider(session, model, userRepository);
	}

	@Override
	public String getId() {
		return "custom-user-lookup-provider";
	}

}
