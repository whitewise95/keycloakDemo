package whitewise.keycloakdemo;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

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

	private Boolean enabled;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public String getFirstName() {
		if (StringUtils.isBlank(this.name) || this.name.length() <= 1){
			return "";
		}

		return this.name.substring(1);
	}

	public String getLastName() {
		if (StringUtils.isBlank(this.name)){
			return "";
		}

		return this.name.substring(0, 1);
	}
}

