package whitewise.keycloakdemo;

import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.MultivaluedHashMap;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import whitewise.keycloakdemo.entity.AccountSocial;
import whitewise.keycloakdemo.entity.AccountUser;

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

	protected AccountUser entity;
	protected String keycloakId;
	protected AccountSocial accountSocial;

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, AccountUser entity) {
		super(session, realm, model);
		this.entity = entity;
		keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getAccount().getId()));
	}

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, AccountUser entity, AccountSocial accountSocial) {
		super(session, realm, model);
		this.entity = entity;
		this.accountSocial = accountSocial;
		keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getAccount().getId()));
	}

	public String getPassword() {
		return entity.getPassword();
	}

	public void setPassword(String password) {
		entity.setPassword(password);
	}

	@Override
	public String getUsername() {
		log.info("entity.getSignName(); : {}", entity.getSignName());
		return entity.getSignName();
	}

	@Override
	public void setUsername(String username) {
		entity.setSignName(username);
	}

	@Override
	public void setEmail(String email) {
		entity.setEmail(email);
	}

	@Override
	public String getEmail() {
		log.info("entity.getEmail(); : {}", entity.getEmail());
		return entity.getEmail();
	}

	@Override
	public String getId() {
		return keycloakId;
	}

	/**
	 * 유저 생성시, 상세에서 수정시 호출
	 * ENABLED, EMAIL_VERIFIED값 셋팅
	 */
	@Override
	public void setSingleAttribute(String name, String value) {
		log.info("setSingleAttribute start name: {}, value : {}", name, value);
		super.setSingleAttribute(name, value);
	}


	@Override
	public void removeAttribute(String name) {
		log.info("removeAttribute start : {}", name);
		super.removeAttribute(name);
	}

	/**
	 * 새로운 유저 생성 및 첫 소셜로그인시 호출됨
	 * 유저의 속성 생성 및 상세수정
	 */
	@Override
	public void setAttribute(String name, List<String> values) {
		log.info("setAttribute name : {}, values : {}", name, values.get(0));
		switch (name.toLowerCase()) {
			case "email" -> entity.setEmail(values.get(0));
			case "name" -> entity.setName(values.get(0));
			case "sub" -> entity.setSignName(values.get(0));
			case "identityprovider" -> {
				accountSocial.setId(new AccountSocial.PK(entity.getAccount().getId(), entity.getAccountSocialType(values.get(0))));
				entity.getAccountSocialList().add(accountSocial);
			}
			default -> super.setAttribute(name, values);
		}
	}

	/**
	 * 유저의 속성 조회
	 * 유저 목록조회시
	 */
	@Override
	public String getFirstAttribute(String name) {
		log.info("getFirstAttribute : {}", name);
		return switch (name.toLowerCase()) {
			case "created_timestamp" -> String.valueOf(entity.getAccount().getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
			case "email_verified" -> "true";
			case "enabled" -> "true";
			case "first_name" -> entity.getName();
			case "last_name" -> entity.getName();
			case "identityprovider" -> "1";
			case "email" -> entity.getEmail(); // 이메일 반환
			default -> super.getFirstAttribute(name);
		};
	}

	/**
	 * 관리자 사이트에서 유저 상세 조회시 호출
	 * 유저 모든 속성 조회
	 */
	@Override
	public Map<String, List<String>> getAttributes() {
		log.info("getAttributes start");
		MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
		all.add("firstName", entity.getName());
		all.add("lastName", entity.getName());
		all.add("email", entity.getEmail());
		all.add("identityprovider", "1");
		all.add("username", entity.getSignName());
		// all.add("sub", entity.getSub());
		return all;
	}

	/**
	 * 유저 생성시 호출
	 */
	@Override
	public Stream<String> getAttributeStream(String name) {
		log.info("getAttributeStream start name: {}", name);
		return super.getAttributeStream(name);
	}

	/**
	 * 기존 유저 ROLE 동기화 및 맵핑
	 */
	@Override
	public Stream<RoleModel> getRoleMappingsStream() {
		log.info("getRoleMappingsStream start");

		Set<RoleModel> roles = new HashSet<>();
		RealmModel realm = session.getContext().getRealm();

		List<String> userRoles = List.of("ROLE_USER");
		for (String roleName : userRoles) {
			RoleModel role = realm.getRole(roleName);

			if (role == null) {
				/**
				 *  keycloak에 없는 ROLE 추가 (RDBMS ROLE)
				 * */
				role = realm.addRole(roleName); // keycloak db role에 추가됨
			}
			roles.add(role);
		}

		Stream<RoleModel> rdbRole = roles.stream();

		/**
		 * keycloak 해당 유저의 할당된 ROLE
		 * */
		Stream<RoleModel> roleModelStream = super.getRoleMappingsStream();

		/**
		 * keycloak ROLE, REDMS ROLE 결합
		 * */
		return Stream.concat(rdbRole, roleModelStream);
	}

	/**
	 * 유저가 특정 role을 가지고 있는지 체크
	 */
	@Override
	public boolean hasRole(RoleModel role) {
		return super.hasRole(role);
	}
}
