package whitewise.keycloakdemo;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class CustomUserLookupProviderFactory implements UserStorageProviderFactory<JdbcUserStorageProvider> {

	public static final String PROVIDER_ID = "example-user-storage-jpa";

	@Override
	public JdbcUserStorageProvider create(KeycloakSession session, ComponentModel model) {
		return new JdbcUserStorageProvider(session, model);
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getHelpText() {
		return "JPA Example User Storage Provider";
	}

	@Override
	public void close() {
	}
}
