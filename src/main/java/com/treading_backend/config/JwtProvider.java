package com.treading_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**This class handles:

 ✅ Creating JWT tokens

 ✅ Extracting email from token

 ✅ Converting authorities (roles) to string

 */

public class JwtProvider
{
	///🔐 Generates a SecretKey from your SECRET_KEY constant.
	private static SecretKey key=Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

	///👉This method creates a JWT token using the user's authentication details
	public static String generateToken(Authentication auth)
	{
///		👉Gets the roles/authorities from the logged-in user and converts them to a comma-separated string.
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
	    String roles = populateAuthorities(authorities);

		String jwt=Jwts.builder()
				.setIssuedAt(new Date())
				.setExpiration(new Date(new Date().getTime()+86400000))
				.claim("email",auth.getName())
				.claim("authorities", roles)
				.signWith(key)
				.compact();
		return jwt;

	///	✅ This builds the JWT like this:
	///		Header: { "alg": "HS256", "typ": "JWT" }
	///		Payload: contains:
	///		email: user email
	///		authorities: e.g., ROLE_ADMIN,ROLE_USER
	///		iat & exp: issued at, expires at
	///		Signature: hashed using your secret key

	}

	/// 👉 Extract the email from the token
	public static String getEmailFromJwtToken(String jwt) {
		jwt=jwt.substring(7);
		
		Claims claims=Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
		String email=String.valueOf(claims.get("email"));
		
		return email;
	}

///     ✅ Converts Spring Security authorities like:
// 			[ROLE_USER , ROLE_ADMIN]

/// 	To a comma-separated string:
//			"ROLE_USER,ROLE_ADMIN"

	public static String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
		Set<String> auths=new HashSet<>();
		
		for(GrantedAuthority authority:collection) {
			auths.add(authority.getAuthority());
		}
		return String.join(",",auths);
	}

}
