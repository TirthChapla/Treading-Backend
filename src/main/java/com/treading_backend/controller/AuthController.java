package com.treading_backend.controller;


import com.treading_backend.config.JwtProvider;
import com.treading_backend.exception.UserException;
import com.treading_backend.model.TwoFactorOTP;
import com.treading_backend.model.User;
import com.treading_backend.repository.UserRepository;
import com.treading_backend.request.LoginRequest;
import com.treading_backend.response.AuthResponse;
import com.treading_backend.service.*;
import com.treading_backend.utils.OtpUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CustomeUserServiceImplementation customUserDetails;
	
	@Autowired
    private UserService userService;

	@Autowired
	private WatchlistService watchlistService;

	@Autowired
	private WalletService walletService;

	@Autowired
	private VerificationService verificationService;

	@Autowired
	private TwoFactorOtpService twoFactorOtpService;

	@Autowired
	private EmailService emailService;


	/// ✅ Signup (Register User)

	///👉 1. Check the provided email is present in Db or not
	///👉 2. save user to DB.
	///👉 3. Create the refrence of Authenticaton via its object : UsernamePasswordAuthenticationToken(email, password)
	///👉 4. we pass this refrence of Authentication to the SPRING.
	///👉 5. Generate a token of this user via :  JwtProvider.generateToken(authentication).
	///👉 6. Return the Authresponse of the user

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> createUserHandler(@RequestBody User user) throws UserException {

		String email = user.getEmail();
		String password = user.getPassword();
		String fullName = user.getFullName();
		String mobile=user.getMobile();

		//👉 check email is in db or not
		User isEmailExist = userRepository.findByEmail(email);

		if (isEmailExist!=null) {

			throw new UserException("Email Is Already Used With Another Account");
		}

		// Create new user
		User createdUser = new User();
		createdUser.setEmail(email);
		createdUser.setFullName(fullName);
		createdUser.setMobile(mobile);
		createdUser.setPassword(passwordEncoder.encode(password));

		//👉 save to DB
		User savedUser = userRepository.save(createdUser);

		watchlistService.createWatchList(savedUser);
//		walletService.createWallet(user);


///		✅ This creates an Authentication object (Spring Security's interface for logged-in users).
///		email: The username (usually the unique identifier for login).
///		password: The plain-text password
		Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);


///		✅ Sets the Authentication object in Spring's security context.
///		🔐 This step marks the user as "logged in" for the current request/session.
		SecurityContextHolder.getContext().setAuthentication(authentication);


///		✅ Calls a method (generateToken) to generate a JWT based on the authentication object.

		///📦 Inside generateToken(...), you likely:
			//👉Extract the email
			//👉Extract roles/authorities
			//👉Add issued time and expiry
			//👉Sign with secret key
		String token = JwtProvider.generateToken(authentication);



		AuthResponse authResponse = new AuthResponse();
		authResponse.setJwt(token);
		authResponse.setMessage("Register Success");

//		{
//  		"jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
//  		"message": "Register Success"
//      }

		return new ResponseEntity<AuthResponse>(authResponse, HttpStatus.OK);

	}





	/// ✅ SIGNIN PROCESS: COMPLETE STEP-BY-STEP SUMMARY
	///
	/// 👉 1. Extract username (email) and password from request
	/// 👉 2. Authenticate credentials using Spring Security
	/// 👉 3. Fetch user details from the database
	/// 👉 4. Set authenticated user into Spring Security context
	/// 👉 5. Generate JWT token
	/// 👉 6. Check if Two-Factor Authentication is enabled
	///     🔹 If enabled:
	///         → Generate a new OTP
	///         → Delete old OTP if present
	///         → Save new OTP with JWT in DB
	///         → Send OTP to user's email
	///         → Return response with OTP session ID
	///     🔹 If not enabled:
	///         → Return response with JWT token only
	///

	@PostMapping("/signin")
	public ResponseEntity<AuthResponse> signing(@RequestBody LoginRequest loginRequest) throws UserException, MessagingException {

		/// 👉 Taking username and pasword
		String username = loginRequest.getEmail();
		String password = loginRequest.getPassword();

		System.out.println(username + " ----- " + password);

		/// 👉 we are cheaking the username and password
		Authentication authentication = authenticate(username, password);

		/// 👉 getting user
		User user=userService.findUserByEmail(username);

		/// 👉 set auth to the spring securiety
		SecurityContextHolder.getContext().setAuthentication(authentication);

		/// 👉 getting JWT token
		String token = JwtProvider.generateToken(authentication);

		/// 👉 We check twofactorAuth is enabled or not if yes then do.
		if(user.getTwoFactorAuth().isEnabled())
		{
			/// 👉 creating AuthResponse
			AuthResponse authResponse = new AuthResponse();
			authResponse.setMessage("Two factor authentication enabled");
			authResponse.setTwoFactorAuthEnabled(true);

			/// 👉 we are generating Random OTP
			String otp= OtpUtils.generateOTP();

			/// 👉 we are finding old twoFactorAuth from db
			TwoFactorOTP oldTwoFactorOTP=twoFactorOtpService.findByUser(user.getId());


			/// ✅✅ TWO FACTOR AUTH✅✅

			/// 👉 if present old twofactorAuth then delete it
			if(oldTwoFactorOTP!=null){
				twoFactorOtpService.deleteTwoFactorOtp(oldTwoFactorOTP);
			}

			/// 👉 creating new twoFactorAuth
			TwoFactorOTP twoFactorOTP=twoFactorOtpService.createTwoFactorOtp(user,otp,token);

			/// 👉 we provide email to the user to check otp
			emailService.sendVerificationOtpEmail(user.getEmail(),otp);

			/// 👉 setting id to session
			authResponse.setSession(twoFactorOTP.getId());

			/// 👉 returning the response
			return new ResponseEntity<>(authResponse, HttpStatus.OK);
		}

		AuthResponse authResponse = new AuthResponse();

		authResponse.setMessage("Login Success");
		authResponse.setJwt(token);

		return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	private Authentication authenticate(String username, String password)
	{
		///👉 This userDetails have : username, password and authorities
		UserDetails userDetails = customUserDetails.loadUserByUsername(username);

		System.out.println("sign in userDetails - " + userDetails);

		if (userDetails == null)
		{
			System.out.println("sign in userDetails - null " + userDetails);
			throw new BadCredentialsException("Invalid username or password");
		}
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			System.out.println("sign in userDetails - password not match " + userDetails);
			throw new BadCredentialsException("Invalid username or password");
		}

		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}




	@GetMapping("/login/google")
	public void redirectToGoogle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// Redirect to the Google OAuth2 authorization URI
		response.sendRedirect("/login/oauth2/authorization/google");
	}

	//	/login/oauth2/code/google
	@GetMapping("/login/oauth2/code/google")
	public User handleGoogleCallback(@RequestParam(required = false,name = "code") String code,
											 @RequestParam(required = false,name = "state") String state,
											 OAuth2AuthenticationToken authentication) {

		// Extract user details from the authentication object or access token
		String email = authentication.getPrincipal().getAttribute("email");
		String fullName = authentication.getPrincipal().getAttribute("name");
		// You can extract more details as needed

		User user=new User();
		user.setEmail(email);
		user.setFullName(fullName);

		return user;
	}

	/// ✅ VERIFY SIGN-IN OTP: STEP-BY-STEP SUMMARY
	///
	/// 👉 1. Endpoint: `/two-factor/otp/{otp}?id=...`
	/// 👉 2. `otp` is passed as a path variable (from the URL path)
	/// 👉 3. `id` (the session ID of TwoFactorOTP) is passed as a request parameter
	/// 👉 4. Fetch the stored TwoFactorOTP from the database using the `id`
	/// 👉 5. Compare the received OTP with the one stored in DB
	/// 👉 6. If OTP matches:
	///     🔹 Return a response with success message, JWT, and 2FA status
	/// 👉 7. If OTP doesn't match:
	///     🔹 Throw an exception with message "invalid otp"

	@PostMapping("/two-factor/otp/{otp}")
	public ResponseEntity<AuthResponse> verifySigningOtp(
			@PathVariable String otp,
			@RequestParam String id  /// 👉 id of twoFactorOtp
	) throws Exception
	{

		///👉 we are retriving twoFactorOTP from DB
		TwoFactorOTP twoFactorOTP = twoFactorOtpService.findById(id);

		///👉 we are comparing DB twoFactorOTP and email vado twoFactorOTP
		if(twoFactorOtpService.verifyTwoFactorOtp(twoFactorOTP,otp))
		{
			AuthResponse authResponse = new AuthResponse();
			authResponse.setMessage("Two factor authentication verified");
			authResponse.setTwoFactorAuthEnabled(true);
			authResponse.setJwt(twoFactorOTP.getJwt());

			///👉 we send Status OKay
			return new ResponseEntity<>(authResponse, HttpStatus.OK);
		}
		throw new Exception("invalid otp");
	}



	
}
