package com.treading_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;




/**This will check token every time when it requestes
 * 1. we extract the header
 * 2. we check is valid or not
 * 3. we extract the TOKEN
 * 4. generate the secret key
 * 5. using Cliams we authenticate the token
 * 6. convert authorities into SimpleGrantedAuthority
 * 7. create the authentication object of user
 * 8. set the authentication to the Spring Securiety
 * 9. after this allow the request to pass if token is correct */

public class JwtTokenValidator extends OncePerRequestFilter
{

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException
	{

		// header link have key value pair is which "Authorization" will contain the token
		String jwt = request.getHeader(JwtConstant.JWT_HEADER);
		
		if(jwt!=null)
		{
			//👉 Removing the "Bearer"
			// Bearer vdbfbvkjbvfwgbvbbcgerigvkvbhwtgadfvklfiherh
			jwt=jwt.substring(7);
			
			
				try
				{
//					✅ This generates a SecretKey using the secret string defined in JwtConstant.SECRET_KEY.
//					🔐 This key is used to verify the signature of the JWT.
					SecretKey key= Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());


//					✅ This line parserBuilder and verifies the JWT token.
//					If valid, it retrieves the Claims (key-value data inside the token).
//					📦 For example: "email": "abc@gmail.com", "authorities": "ROLE_USER"

					Claims claims=Jwts
							.parserBuilder()
							.setSigningKey(key)
							.build()
							.parseClaimsJws(jwt)
							.getBody();

					//✅ Extracts the email from the JWT payload.
					String email=String.valueOf(claims.get("email"));

					//✅ Extracts the roles/authorities string from JWT (e.g., "ROLE_ADMIN,ROLE_USER").
					String authorities=String.valueOf(claims.get("authorities"));

					System.out.println("authorities -------- "+authorities);


					//✅ Converts comma-separated roles into Spring Security's GrantedAuthority list.
					//📍 Example:
					// "ROLE_ADMIN,ROLE_USER" → [new SimpleGrantedAuthority("ROLE_ADMIN"), ...]
					List<GrantedAuthority> auths=AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

					//✅ Creates an Authentication object for the current user.
					//🔑 Username is email, password is null, authorities are roles.
					Authentication athentication=new UsernamePasswordAuthenticationToken(email,null, auths);


					//✅ Sets this authentication into the Spring Security Context, so the user is now "logged in" for this request.
					SecurityContextHolder.getContext().setAuthentication(athentication);

				} catch (Exception e) {
					throw new RuntimeException("invalid token...");
				}
		}


		filterChain.doFilter(request, response);
		
	}



}
