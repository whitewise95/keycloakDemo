package whitewise.keycloakdemo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

	private static final Logger log = LoggerFactory.getLogger(AbstractUserAdapterFederatedStorage.class);

	protected UserEntity entity;
	protected String keycloakId;

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, UserEntity entity) {
		super(session, realm, model);
		this.entity = entity;
		keycloakId = StorageId.keycloakId(model, String.valueOf(entity.getId()));
	}

	@Override
	public String getId() {
		log.info("getId called for entity: {}", entity.getId().toString());
		return entity.getId().toString();
	}

	@Override
	public String getUsername() {
		log.info("getUsername called for entity: {}", entity.getUsername());
		return entity.getUsername();
	}

	@Override
	public void setUsername(String s) {
		entity.setUsername(s);
	}

	@Override
	public Long getCreatedTimestamp() {
		return LocalDateTime.now()
							.atZone(ZoneId.systemDefault()) // 시스템의 기본 시간대 적용
							.toInstant()                   // Instant로 변환
							.toEpochMilli();               // 밀리초로 변환
	}

	@Override
	public void setCreatedTimestamp(Long aLong) {
	}

	@Override
	public boolean isEnabled() {
		return entity.getEnabled() != null && entity.getEnabled();
	}

	@Override
	public void setEnabled(boolean b) {

	}

	public String getPassword() {
		return entity.getPassword();
	}

	@Override
	public String getEmail() {
		log.info("getEmail called for entity: {}", entity.getUsername());
		return entity.getEmail();
	}

	@Override
	public void setEmail(String s) {

	}
}
