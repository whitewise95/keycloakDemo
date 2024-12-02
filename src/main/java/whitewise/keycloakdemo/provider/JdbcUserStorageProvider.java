package whitewise.keycloakdemo.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.CredentialValidationOutput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import whitewise.keycloakdemo.entity.User;
import whitewise.keycloakdemo.entity.UserRepository;
import whitewise.keycloakdemo.keycloak.CustomUserModel;

public class JdbcUserStorageProvider implements UserLookupProvider, UserStorageProvider, CredentialInputValidator {

	private final KeycloakSession session;
	private final ComponentModel model;
	private final UserRepository userRepository;

	public JdbcUserStorageProvider(KeycloakSession session, ComponentModel model, UserRepository userRepository) {
		this.session = session;
		this.model = model;
		this.userRepository = userRepository;
	}

	@Override
	public UserModel getUserById(RealmModel realmModel, String id) {
		User user = userRepository.findById(Long.valueOf(id)).orElseThrow(RuntimeException::new);
		return new CustomUserModel(user, realmModel);
	}

	@Override
	public UserModel getUserByUsername(RealmModel realmModel, String username) {
		User user = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);
		return new CustomUserModel(user, realmModel);
	}

	@Override
	public CredentialValidationOutput getUserByCredential(RealmModel realm, CredentialInput input) {
		return UserLookupProvider.super.getUserByCredential(realm, input);
	}

	@Override
	public UserModel getUserByEmail(RealmModel realmModel, String email) {
		User user = userRepository.findByEmail(email).orElseThrow(RuntimeException::new);
		return new CustomUserModel(user, realmModel);
	}

	@Override
	public void close() {
		// 사용자 정의 스토리지 프로바이더를 종료할 때 호출 리소스를 정리하거나 필요한 연산이 있을 때 사용
	}

	@Override
	public boolean supportsCredentialType(String credentialType) {
		return "password".equals(credentialType); // password만 지원
	}

	@Override
	public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
		CustomUserModel customUser = (CustomUserModel) userModel;

		if (!(userModel instanceof CustomUserModel)) {
			return false; // CustomUserModel이 아닌 경우 처리하지 않음
		}

		return supportsCredentialType(credentialType) && customUser.getFirstAttribute("password") != null;
	}

	@Override
	public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
		if (!(userModel instanceof CustomUserModel)) {
			return false; // CustomUserModel이 아닌 경우 처리하지 않음
		}

		CustomUserModel customUser = (CustomUserModel) userModel;

		if (!supportsCredentialType(credentialInput.getType())) {
			return false; // 비밀번호 이외의 Credential은 처리하지 않음
		}

		String rawPassword = credentialInput.getChallengeResponse(); // 사용자가 입력한 비밀번호
		String storedPassword = customUser.getFirstAttribute("password"); // 저장된 비밀번호
		return rawPassword.equals(storedPassword); // 실제로는 BCrypt
	}
}
