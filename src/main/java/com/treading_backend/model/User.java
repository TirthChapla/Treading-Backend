package com.treading_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.treading_backend.domain.USER_ROLE;
import com.treading_backend.domain.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String fullName;
	private String email;
	private String mobile;

///	@JsonProperty ( access = JsonProperty.Access.WRITE_ONLY) means: The field will be deserialized from JSON (i.e., it will be set when receiving data, like from a request body).  But it will NOT be serialized to JSON (i.e., it won't be shown in the response).

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	
	private UserStatus status= UserStatus.PENDING;

	private boolean isVerified = false;

///@Embedded :  It tells JPA that this field is not a separate entity, but its fields should be mapped as part of the current entity's table.  The class used must be annotated with @Embeddable.  Use when:  The fields logically belong to the entity. They are shared across multiple entities. You don't need a separate table for them.

	@Embedded
	private TwoFactorAuth twoFactorAuth= new TwoFactorAuth();

	private String picture;

	private USER_ROLE role= USER_ROLE.ROLE_USER;

}
