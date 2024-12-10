package whitewise.keycloakdemo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcUserStorageProvider implements UserStorageProvider,
	UserLookupProvider,
	UserRegistrationProvider,
	UserQueryProvider,
	CredentialInputUpdater,
	CredentialInputValidator,
	OnUserCache {

	public static final String PASSWORD_CACHE_KEY = UserAdapter.class.getName() + ".password";
	private static final Logger log = LoggerFactory.getLogger(JdbcUserStorageProvider.class);
	protected EntityManager em;
	private final KeycloakSession session;
	private final ComponentModel model;

	public JdbcUserStorageProvider(KeycloakSession session, ComponentModel model) {
		this.session = session;
		this.model = model;
		em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
	}

	@Override
	public boolean supportsCredentialType(String credentialType) {
		log.info("credentialType : {}", credentialType);
		return PasswordCredentialModel.TYPE.equals(credentialType);
	}

	@Override
	public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
		log.info("isConfiguredFor : {}", userModel);
		return supportsCredentialType(credentialType) && getPassword(userModel) != null;
	}

	public String getPassword(UserModel user) {
		log.info("getPassword : {}", user);
		String password = null;
		if (user instanceof CachedUserModel) {
			password = (String) ((CachedUserModel) user).getCachedWith().get(PASSWORD_CACHE_KEY);
		} else if (user instanceof UserAdapter) {
			password = ((UserAdapter) user).getPassword();
		}
		return password;
	}

	@Override
	public boolean updateCredential(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
		return false;
	}

	@Override
	public void disableCredentialType(RealmModel realmModel, UserModel userModel, String s) {

	}

	@Override
	public Stream<String> getDisableableCredentialTypesStream(RealmModel realmModel, UserModel userModel) {
		return Stream.empty();
	}

	@Override
	public void onCache(RealmModel realmModel, CachedUserModel user, UserModel delegate) {
		log.info("onCache : {}", user);
		String password = ((UserAdapter) delegate).getPassword();
		if (password != null) {
			user.getCachedWith().put(PASSWORD_CACHE_KEY, password);
		}
	}

	@Override
	public UserModel getUserById(RealmModel realmModel, String id) {
		log.info("id : {}", id);
		String persistenceId = StorageId.externalId(id);
		log.info("persistenceId : {}", persistenceId);
		UserEntity entity = em.find(UserEntity.class, persistenceId);
		if (entity == null) {
			return null;
		}
		log.info("entity : {}", entity);
		return new UserAdapter(session, realmModel, model, entity);
	}

	@Override
	public UserModel getUserByUsername(RealmModel realmModel, String username) {
		TypedQuery<UserEntity> query = em.createNamedQuery("getUserByUsername", UserEntity.class);
		query.setParameter("username", username);
		List<UserEntity> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		}

		return new UserAdapter(session, realmModel, model, result.get(0));
	}

	@Override
	public UserModel getUserByEmail(RealmModel realmModel, String email) {
		TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
		query.setParameter("email", email);
		List<UserEntity> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		}
		return new UserAdapter(session, realmModel, model, result.get(0));
	}

	@Override
	public void close() {

	}

	@Override
	public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> params, Integer firstResult, Integer maxResults) {
		String search = params.get(UserModel.SEARCH);
		TypedQuery<UserEntity> query = em.createNamedQuery("searchForUser", UserEntity.class);
		log.info("info search : {}", search);
		String lower = search != null ? search.toLowerCase() : "";
		query.setParameter("search", "%" + lower + "%");
		if (firstResult != null) {
			query.setFirstResult(firstResult);
		}
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		return query.getResultStream().map(entity -> {
			log.info("error getResultStream : {}", entity.getId());
			return new UserAdapter(session, realmModel, model, entity);
		});
	}

	@Override
	public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
		return Stream.empty();
	}

	@Override
	public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
		return Stream.empty();
	}

	@Override
	public UserModel addUser(RealmModel realmModel, String username) {
		log.info("realmModel : {}, username : {}", realmModel, username);
		UserEntity entity = new UserEntity();
		entity.setUsername(username);
		entity.setEmail("test@test");
		entity.setPassword("1234");
		entity.setEnabled(true);
		em.persist(entity);
		return new UserAdapter(session, realmModel, model, entity);
	}

	@Override
	public boolean removeUser(RealmModel realmModel, UserModel userModel) {
		log.info("removeUser : {}", userModel);
		String persistenceId = StorageId.externalId(userModel.getId());
		UserEntity entity = em.find(UserEntity.class, persistenceId);
		if (entity == null) {
			return false;
		}
		em.remove(entity);
		return true;
	}

	@Override
	public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
		log.info("credentialInput : {}", credentialInput);
		if (!(userModel instanceof UserEntity)) {
			return false; // CustomUserModel이 아닌 경우 처리하지 않음
		}

		UserEntity customUser = (UserEntity) userModel;

		if (!supportsCredentialType(credentialInput.getType())) {
			return false; // 비밀번호 이외의 Credential은 처리하지 않음
		}

		String rawPassword = credentialInput.getChallengeResponse(); // 사용자가 입력한 비밀번호
		String storedPassword = customUser.getPassword(); // 저장된 비밀번호
		return rawPassword.equals(storedPassword); // 실제로는 BCrypt
	}
}
