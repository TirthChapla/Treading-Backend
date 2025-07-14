package com.treading_backend.service;

import com.treading_backend.model.User;
import com.treading_backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



/**This will check the user is present in database or Not */

@Service
public class CustomeUserServiceImplementation implements UserDetailsService {


	private UserRepository userRepository;

	//Construction Autowired
	public CustomeUserServiceImplementation(UserRepository userRepository)
	{
		this.userRepository=userRepository;
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// our username = email
		User user = userRepository.findByEmail(username);
		
		if(user==null) {

			throw new UsernameNotFoundException("user not found with email  - "+username);
		}

		/// 👉 (like "ADMIN" or "USER")
		List<GrantedAuthority> authorities=new ArrayList<>();

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(),user.getPassword(),authorities);
	}


}
