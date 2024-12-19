package whitewise.keycloakdemo.entity;

import com.google.common.collect.Sets;
import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "account")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;

	public enum AccountType {
		ADMIN("관리자"),
		USER("사용자");

		public final String label;

		AccountType(String label) {
			this.label = label;
		}
	}

	/**
	 * 계정타입
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	private AccountType accountType;

	/**
	 * 비밀번호 변경일시
	 */
	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	private LocalDateTime passwordChangeAt = LocalDateTime.now();

	/**
	 * 비밀번호 불일치 횟수
	 */
	@NotNull
	@ColumnDefault("0")
	private Integer passwordLockCount = 0;

	/**
	 * 잠금여부
	 */
	@NotNull
	@ColumnDefault("false")
	private Boolean isLock = false;


	/**
	 * 최종로그인시간
	 */
	private LocalDateTime lastLoginAt;

	/**
	 * 최종로그인IP
	 */
	private String lastLoginIp;

	/**
	 *  생성일
	 */
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<AccountUser> accountUserList = Sets.newHashSet();
}
