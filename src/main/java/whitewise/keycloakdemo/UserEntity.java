package whitewise.keycloakdemo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@NamedQueries({
	@NamedQuery(name = "getUserByUsername", query = "select u from CustomUserEntity u where u.username = :username"),
	@NamedQuery(name = "getUserById", query = "select u from CustomUserEntity u where u.id = :id"),
	@NamedQuery(name = "getUserByEmail", query = "select u from CustomUserEntity  u where u.email = :email"),
	@NamedQuery(name = "getUserCount", query = "select count(u) from CustomUserEntity  u"),
	@NamedQuery(name = "getAllUsers", query = "select u from CustomUserEntity  u"),
	@NamedQuery(name = "searchForUser", query = "select u from CustomUserEntity  u where " +
		"( lower(u.username) like :search or u.email like :search ) order by u.username"),
})
@Entity(name = "CustomUserEntity")
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private Boolean enabled;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
}

