package whitewise.keycloakdemo;

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

@Slf4j
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

	protected UserEntity entity;
	protected String keycloakId;

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserEntity entity) {
		super(session, realm, model);
		this.entity = entity;
		keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getId()));
	}

	public String getPassword() {
		return entity.getPassword();
	}

	public void setPassword(String password) {
		entity.setPassword(password);
	}

	@Override
	public String getUsername() {
		return entity.getUsername();
	}

	@Override
	public void setUsername(String username) {
		entity.setUsername(username);
	}

	@Override
	public void setEmail(String email) {
		entity.setEmail(email);
	}

	@Override
	public String getEmail() {
		return entity.getEmail();
	}

	@Override
	public String getId() {
		return keycloakId;
	}

	/**
 	 * 유저 생성시, 상세에서 수정시 호출
	 * ENABLED, EMAIL_VERIFIED값 셋팅
	 * */
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
			case "firstname" -> entity.setName(entity.getLastName() + values.get(0));
			case "lastname" -> entity.setName(values.get(0) + entity.getName());
			case "email" -> entity.setEmail(values.get(0));
			case "sub" -> entity.setSub(values.get(0));
			case "iss" -> entity.setIss(values.get(0));
			case "name" -> entity.setName(values.get(0));
			case "emailverified" -> entity.setEmailVerified(Boolean.valueOf(values.get(0)));
			default -> super.setAttribute(name, values);
		}
	}

	/**
	 * 유저의 속성 조회
	 * 유저 목록조회시 상세조회시 호출
	 */
	@Override
	public String getFirstAttribute(String name) {
		log.info("getFirstAttribute : {}", name);
		return switch (name.toLowerCase()) {
			case "created_timestamp" -> String.valueOf(entity.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
			case "email_verified" -> entity.getEmail();
			case "enabled" -> entity.getEnabled().toString();
			case "firstname" -> entity.getFirstName();
			case "lastname" -> entity.getLastName();
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
		all.add("firstName", entity.getFirstName());
		all.add("lastName", entity.getLastName());
		all.add("email", entity.getEmail());
		all.add("username", entity.getUsername());
		return all;
	}

	/**
	 * 유저 생성시 호출
	 * */
	@Override
	public Stream<String> getAttributeStream(String name) {
		log.info("getAttributeStream start name: {}", name);
		return super.getAttributeStream(name);
	}

	/**
	 * ROLE 맵핑
	 * */
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
				 * keycloak에 없는 ROLE 맵핑 (RDBMS ROLE)
				 * */
				role = realm.addRole(roleName);
			}
			roles.add(role);
		}

		Stream<RoleModel> rdbRole = roles.stream();
		//endregion

		/**
		 * keycloak 해당 유저의 할당된 ROLE
		 * */
		Stream<RoleModel> roleModelStream = super.getRoleMappingsStream();

		/**
		 * keycloak ROLE, REDMS ROLE 합
		 * */
		return Stream.concat(rdbRole, roleModelStream);
	}

	@Override
	public boolean hasRole(RoleModel role) {
		log.info("hasRole param: {}", role);

		boolean b = super.hasRole(role);
		log.info("hasRole result: {}", b);
		return b;
	}
}
