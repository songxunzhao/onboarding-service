package ee.tuleva.onboarding.user;

import ee.tuleva.onboarding.auth.principal.Person;
import lombok.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

@Data
@Builder
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude={"member"})
public class User implements Person, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(cascade = {CascadeType.ALL}, mappedBy = "user")
	Member member;

	@NotBlank
	@Size(min = 11, max = 11)
	private String personalCode;

	@NotNull
	@Email
	private String email;

	private String phoneNumber;

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	@NotNull
	@Past
	private Instant createdDate;

	@NotNull
	private Instant updatedDate;

	@NotNull
	private Boolean active;

	public int getAge() {
		return PersonalCode.getAge(personalCode);
	}

	public Optional<Member> getMember() {
		return Optional.ofNullable(member);
	}

}
