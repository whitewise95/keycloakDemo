package whitewise.keycloakdemo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@NamedQueries({
	@NamedQuery(name = "getUserByUsername", query = "select u from UserEntity u where u.username = :username"),
	@NamedQuery(name = "getUserById", query = "select u from UserEntity u where u.id = :id"),
	@NamedQuery(name = "getUserByEmail", query = "select u from UserEntity  u where u.email = :email"),
	@NamedQuery(name = "getUserCount", query = "select count(u) from UserEntity  u"),
	@NamedQuery(name = "getAllUsers", query = "select u from UserEntity  u"),
	@NamedQuery(name = "searchForUser", query = "select u from UserEntity  u where " +
		"( lower(u.username) like :search or u.email like :search ) order by u.username"),
})
@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	private String username;

	private String name;

	private String email;

	private String password;

	private String phone;

	@Column(nullable = false)
	private Boolean enabled;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public String getFirstName() {
		return this.name.substring(1);
	}

	public String getLastName() {
		return this.name.substring(0,1);
	}
}

