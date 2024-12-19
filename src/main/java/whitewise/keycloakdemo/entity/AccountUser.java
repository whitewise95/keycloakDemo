package whitewise.keycloakdemo.entity;

import com.google.common.collect.Lists;
import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 사용자계정
 */
@NamedQueries({
	@NamedQuery(
		name = "getUserByUsername",
		query = "SELECT au FROM AccountUser au inner join AccountSocial ass on au.account.id = ass.accountUser.account.id and ass.signName = :username"
	),
	@NamedQuery(
		name = "getUserById",
		query = "SELECT au FROM AccountUser au WHERE au.account.id = :id"
	),
	@NamedQuery(
		name = "getUserByEmail",
		query = "SELECT au FROM AccountUser au WHERE au.email = :email"
	),
	@NamedQuery(
		name = "getUserCount",
		query = "SELECT COUNT(au) FROM AccountUser au"
	),
	@NamedQuery(
		name = "getAllUsers",
		query = "SELECT au FROM AccountUser au"
	),
	@NamedQuery(
		name = "searchForUser",
		query = "SELECT au FROM AccountUser au " +
			"WHERE LOWER(au.name) LIKE :search OR au.email LIKE :search " +
			"ORDER BY au.name"
	)
})
@Getter
@Setter
@Entity
public class AccountUser {

	private static final Logger log = LoggerFactory.getLogger(AccountUser.class);
	/**
	 * 계정마스터 고유번호
	 */
	@Id
	@Column(name = "account_id")
	private Long id;

	/**
	 * 계정마스터
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(unique = true)
	@MapsId //@MapsId 는 @id로 지정한 컬럼에 @OneToOne 이나 @ManyToOne 관계를 매핑시키는 역할
	private Account account;

	/**
	 * 이름
	 */
	private String name;

	/**
	 * 이메일
	 */
	private String email;

	/**
	 * 생년월일
	 */
	private String birth;

	/**
	 * 사용자계정 연동계정
	 */
	@OrderBy("id.accountUserId DESC")
	@OneToMany(mappedBy = "id.accountUserId", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccountSocial> accountSocialList = Lists.newArrayList();


	public String getSignName() {
		return this.accountSocialList
			.stream()
			.findAny()
			.map(AccountSocial::getSignName)
			.orElseThrow(() -> new RuntimeException("Sign name not found"));
	}

	public void setSignName(String signName) {
		this.accountSocialList
			.stream()
			.findAny()
			.ifPresent(x -> x.setSignName(signName));
	}

	public String getPassword() {
		log.info("getPassword size : {}", this.accountSocialList.size());
		return this.accountSocialList
			.stream()
			.findAny()
			.map(AccountSocial::getPassword).orElse(null);
	}

	public void setPassword(String password) {
		this.accountSocialList
			.stream()
			.filter(x -> x.getId().getType().equals(AccountSocial.PK.AccountSocialType.KEYCLOAK))
			.findAny()
			.ifPresent(x -> x.setPassword(password));
	}

	public AccountSocial.PK.AccountSocialType getAccountSocialType(String identityProvider) {
		return switch (identityProvider) {
			case "google" -> AccountSocial.PK.AccountSocialType.GOOGLE;
			case "kakao" -> AccountSocial.PK.AccountSocialType.KAKAO;
			default -> AccountSocial.PK.AccountSocialType.KEYCLOAK;
		};
	}
}
