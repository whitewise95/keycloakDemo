package whitewise.keycloakdemo.provider;

import java.sql.Connection;
import lombok.RequiredArgsConstructor;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.models.CredentialValidationOutput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import whitewise.keycloakdemo.entity.User;
import whitewise.keycloakdemo.entity.UserRepository;

public class JdbcUserStorageProvider implements UserLookupProvider, UserStorageProvider {

	private final KeycloakSession session;
	private final ComponentModel model;
	private final UserRepository userRepository;

	public JdbcUserStorageProvider(KeycloakSession session, ComponentModel model, UserRepository userRepository) {
		this.session = session;
		this.model = model;
		this.userRepository = userRepository;
	}

	@Override
	public UserModel getUserById(RealmModel realmModel, String username) {
		User user = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);

		return null;
	}

	@Override
	public UserModel getUserByUsername(RealmModel realmModel, String s) {
		return null;
	}

	@Override
	public CredentialValidationOutput getUserByCredential(RealmModel realm, CredentialInput input) {
		return UserLookupProvider.super.getUserByCredential(realm, input);
	}

	@Override
	public UserModel getUserByEmail(RealmModel realmModel, String s) {
		return null;
	}

	@Override
	public void close() {

	}
}
