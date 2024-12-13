package whitewise.keycloakdemo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
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
	private static final Logger logger = LoggerFactory.getLogger(JdbcUserStorageProvider.class);
	protected EntityManager em;
	private final KeycloakSession session;
	private final ComponentModel model;

	public JdbcUserStorageProvider(KeycloakSession session, ComponentModel model) {
		this.session = session;
		this.model = model;
		em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
	}

	@Override
	public UserModel getUserById(RealmModel realm, String id) {
		logger.info("getUserById: " + id);
		String persistenceId = StorageId.externalId(id);
		UserEntity entity = em.find(UserEntity.class, persistenceId);
		if (entity == null) {
			logger.info("could not find user by id: " + id);
			return null;
		}
		return new UserAdapter(session, realm, model, entity);
	}

	@Override
	public UserModel getUserByUsername(RealmModel realm, String username) {
		logger.info("getUserByUsername: " + username);
		TypedQuery<UserEntity> query = em.createNamedQuery("getUserByUsername", UserEntity.class);
		query.setParameter("username", username);
		List<UserEntity> result = query.getResultList();
		if (result.isEmpty()) {
			logger.info("could not find username: " + username);
			return null;
		}

		return new UserAdapter(session, realm, model, result.get(0));
	}

	@Override
	public UserModel getUserByEmail(RealmModel realm, String email) {
		TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
		query.setParameter("email", email);
		List<UserEntity> result = query.getResultList();
		if (result.isEmpty()) return null;
		return new UserAdapter(session, realm, model, result.get(0));
	}

	@Override
	public UserModel addUser(RealmModel realm, String username) {
		UserEntity entity = new UserEntity();
		entity.setUsername(username);
		entity.setCreatedAt(LocalDateTime.now());
		entity.setEnabled(true);
		em.persist(entity);
		logger.info("added user: " + username);
		return new UserAdapter(session, realm, model, entity);
	}

	@Override
	public boolean removeUser(RealmModel realm, UserModel user) {
		String persistenceId = StorageId.externalId(user.getId());
		UserEntity entity = em.find(UserEntity.class, persistenceId);
		if (entity == null) return false;
		em.remove(entity);
		return true;
	}

	@Override
	public void onCache(RealmModel realm, CachedUserModel user, UserModel delegate) {
		String password = ((UserAdapter)delegate).getPassword();
		if (password != null) {
			user.getCachedWith().put(PASSWORD_CACHE_KEY, password);
		}
	}

	@Override
	public boolean supportsCredentialType(String credentialType) {
		return PasswordCredentialModel.TYPE.equals(credentialType);
	}

	@Override
	public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
		if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) return false;
		UserCredentialModel cred = (UserCredentialModel)input;
		UserAdapter adapter = getUserAdapter(user);
		adapter.setPassword(cred.getValue());

		return true;
	}

	public UserAdapter getUserAdapter(UserModel user) {
		if (user instanceof CachedUserModel) {
			return (UserAdapter)((CachedUserModel) user).getDelegateForUpdate();
		} else {
			return (UserAdapter) user;
		}
	}

	@Override
	public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
		if (!supportsCredentialType(credentialType)) return;

		getUserAdapter(user).setPassword(null);

	}

	@Override
	public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
		if (getUserAdapter(user).getPassword() != null) {
			Set<String> set = new HashSet<>();
			set.add(PasswordCredentialModel.TYPE);
			return set.stream();
		} else {
			return Stream.empty();
		}
	}

	/**
	 * 인증 서포트 타입을 확인 현재는 Password 방식으로 되어 있음 ex) otp, WebAuthn 등등
	 * */
	@Override
	public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
		return supportsCredentialType(credentialType) && getPassword(user) != null;
	}

	/**
	 * 로그인시 패스워드 valid
	 * */
	@Override
	public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
		logger.info("isValid", "isValid");
		if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) return false;
		UserCredentialModel cred = (UserCredentialModel)input;
		String password = getPassword(user);
		return password != null && password.equals(cred.getValue());
	}


	public String getPassword(UserModel user) {
		String password = null;
		if (user instanceof CachedUserModel) {
			password = (String)((CachedUserModel)user).getCachedWith().get(PASSWORD_CACHE_KEY);
		} else if (user instanceof UserAdapter) {
			password = ((UserAdapter)user).getPassword();
		}
		return password;
	}

	/**
	 * keycloak admin 콘솔에서 user 목록 조회할 때 사용
	 * */
	@Override
	public int getUsersCount(RealmModel realm) {
		Object count = em.createNamedQuery("getUserCount")
						 .getSingleResult();
		return ((Number)count).intValue();
	}

	/**
	 * keycloak admin 콘솔에서 user 목록 조회할 때 사용
	 * */
	@Override
	public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
		String search = params.get(UserModel.SEARCH);
		TypedQuery<UserEntity> query = em.createNamedQuery("searchForUser", UserEntity.class);
		String lower = search != null ? search.toLowerCase() : "";
		query.setParameter("search", "%" + lower + "%");
		if (firstResult != null) {
			query.setFirstResult(firstResult);
		}
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		return query.getResultStream().map(entity -> new UserAdapter(session, realm, model, entity));
	}

	@Override
	public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
		return Stream.empty();
	}

	@Override
	public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
		return Stream.empty();
	}

	@Override
	public void close() {
	}
}
