package whitewise.keycloakdemo.entity;

import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자계정 연동계정
 */
@Getter
@Setter
@Entity
public class AccountSocial {

	/**
	 * 복합키
	 */
	@EmbeddedId
	private PK id;

	@Embeddable
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class PK implements Serializable {

		/**
		 * 사용자계정 고유번호
		 */
		private Long accountUserId;


		public enum AccountSocialType {
			KAKAO("카카오", "kakao@"),
			KEYCLOAK("keycloak", "keycloak@"),
			GOOGLE("구글", "google@");

			public final String label;
			public final String prefix;

			AccountSocialType(String label, String prefix) {
				this.label = label;
				this.prefix = prefix;
			}
		}

		/**
		 * 계정타입
		 */
		@Enumerated(EnumType.STRING)
		private AccountSocialType type;
	}

	/**
	 * 사용자계정 Maps
	 */
	@MapsId("accountUserId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private AccountUser accountUser;

	/**
	 * 고유키
	 */
	@NotNull
	private String signName;

	/**
	 * 연동계정 비밀번호
	 */
	@NotNull
	private String password;

	@PrePersist
	public void generateSignNameForSocial() {
		if (!this.getId().getType().equals(PK.AccountSocialType.KEYCLOAK)){
			this.signName = this.getId().getType().prefix + this.signName;
		}
	}
}
