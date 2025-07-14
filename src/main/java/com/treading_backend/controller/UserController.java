package com.treading_backend.controller;

import com.treading_backend.domain.VerificationType;
import com.treading_backend.exception.UserException;
import com.treading_backend.model.ForgotPasswordToken;
import com.treading_backend.model.User;
import com.treading_backend.model.VerificationCode;
import com.treading_backend.request.ResetPasswordRequest;
import com.treading_backend.request.UpdatePasswordRequest;
import com.treading_backend.response.ApiResponse;
import com.treading_backend.response.AuthResponse;
import com.treading_backend.service.EmailService;
import com.treading_backend.service.ForgotPasswordService;
import com.treading_backend.service.UserService;
import com.treading_backend.service.VerificationService;
import com.treading_backend.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
public class UserController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private VerificationService verificationService;

	@Autowired
	private ForgotPasswordService forgotPasswordService;

	@Autowired
	private EmailService emailService;


	@GetMapping("/api/users/profile")
	public ResponseEntity<User> getUserProfileHandler(
			@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.findUserProfileByJwt(jwt);
		user.setPassword(null);

		return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
	}
	
	@GetMapping("/api/users/{userId}")
	public ResponseEntity<User> findUserById(
			@PathVariable Long userId,
			@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.findUserById(userId);
		user.setPassword(null);

		return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
	}

	@GetMapping("/api/users/email/{email}")
	public ResponseEntity<User> findUserByEmail(
			@PathVariable String email,
			@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.findUserByEmail(email);

		return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	///❤️ HTTP Method	                 Use Case

	///     POST	         Create a new resource
	///     PUT	             Replace an entire resource
	///    PATCH	         Update part of a resource
	///
	/// ✅ When to Use @PatchMapping:
	/// When you want to partially update fields of an entity (e.g., just the status, name, or email).
	/// Ideal for UI forms that edit a small part of the data.

	/// ✅ ENABLE TWO-FACTOR AUTHENTICATION - VERIFY OTP
	///
	/// 👉 1. Endpoint: `PATCH /api/users/enable-two-factor/verify-otp/{otp}`
	/// 👉 2. JWT is passed via request header for user identification
	/// 👉 3. OTP is passed as a path variable
	/// 👉 4. Extract user details from JWT
	/// 👉 5. Fetch the user's verification data (OTP, type: EMAIL/MOBILE)
	/// 👉 6. Verify the entered OTP against the stored one
	/// 👉 7. If verified:
	///     🔹 Enable two-factor auth for user (via EMAIL or MOBILE)
	///     🔹 Delete the OTP record
	///     🔹 Return the updated user
	/// 👉 8. If OTP is wrong:
	///

	@PatchMapping("/api/users/enable-two-factor/verify-otp/{otp}")
	public ResponseEntity<User> enabledTwoFactorAuthentication(
			@RequestHeader("Authorization") String jwt,
			@PathVariable String otp
	) throws Exception
	 {
		/// 👉  get user by JWT
		User user = userService.findUserProfileByJwt(jwt);

		///👉 we are getting verification code from the DB
		VerificationCode verificationCode = verificationService.findUsersVerification(user);

		///👉 this will tell to verify via Email or Mobile No.
		String sendTo=verificationCode.getVerificationType().equals(VerificationType.EMAIL)?verificationCode.getEmail():verificationCode.getMobile();

		///👉 it will check by comparing DB OTP and user entered OTP
		boolean isVerified = verificationService.VerifyOtp(otp, verificationCode);

		if (isVerified)
		{
			User updatedUser = userService.enabledTwoFactorAuthentication(verificationCode.getVerificationType(),
					sendTo,user);
			verificationService.deleteVerification(verificationCode);
			return ResponseEntity.ok(updatedUser);
		}
		throw new Exception("wrong otp");

	}



	@PatchMapping("/auth/users/reset-password/verify-otp")
	public ResponseEntity<ApiResponse> resetPassword(
			@RequestParam String id,
			@RequestBody ResetPasswordRequest req
			) throws Exception {
		ForgotPasswordToken forgotPasswordToken=forgotPasswordService.findById(id);

			boolean isVerified = forgotPasswordService.verifyToken(forgotPasswordToken,req.getOtp());

			if (isVerified) {

				userService.updatePassword(forgotPasswordToken.getUser(),req.getPassword());
				ApiResponse apiResponse=new ApiResponse();
				apiResponse.setMessage("password updated successfully");
				return ResponseEntity.ok(apiResponse);
			}
			throw new Exception("wrong otp");

	}

	@PostMapping("/auth/users/reset-password/send-otp")
	public ResponseEntity<AuthResponse> sendUpdatePasswordOTP(
			@RequestBody UpdatePasswordRequest req)
			throws Exception {

		User user = userService.findUserByEmail(req.getSendTo());
		String otp= OtpUtils.generateOTP();
		UUID uuid = UUID.randomUUID();
		String id = uuid.toString();

		ForgotPasswordToken token = forgotPasswordService.findByUser(user.getId());

		if(token==null){
			token=forgotPasswordService.createToken(
					user,id,otp,req.getVerificationType(), req.getSendTo()
			);
		}

		if(req.getVerificationType().equals(VerificationType.EMAIL)){
			emailService.sendVerificationOtpEmail(
					user.getEmail(),
					token.getOtp()
			);
		}

		AuthResponse res=new AuthResponse();
		res.setSession(token.getId());
		res.setMessage("Password Reset OTP sent successfully.");

		return ResponseEntity.ok(res);

	}

	@PatchMapping("/api/users/verification/verify-otp/{otp}")
	public ResponseEntity<User> verifyOTP(
			@RequestHeader("Authorization") String jwt,
			@PathVariable String otp
	) throws Exception {


		User user = userService.findUserProfileByJwt(jwt);


		VerificationCode verificationCode = verificationService.findUsersVerification(user);


		boolean isVerified = verificationService.VerifyOtp(otp, verificationCode);

		if (isVerified) {
			verificationService.deleteVerification(verificationCode);
			User verifiedUser = userService.verifyUser(user);
			return ResponseEntity.ok(verifiedUser);
		}
		throw new Exception("wrong otp");

	}

	@PostMapping("/api/users/verification/{verificationType}/send-otp")
	public ResponseEntity<String> sendVerificationOTP(
			@PathVariable VerificationType verificationType,
			@RequestHeader("Authorization") String jwt)
            throws Exception {

		User user = userService.findUserProfileByJwt(jwt);

		VerificationCode verificationCode = verificationService.findUsersVerification(user);

		if(verificationCode == null) {
			verificationCode = verificationService.sendVerificationOTP(user,verificationType);
		}


		if(verificationType.equals(VerificationType.EMAIL)){
			emailService.sendVerificationOtpEmail(user.getEmail(), verificationCode.getOtp());
		}



		return ResponseEntity.ok("Verification OTP sent successfully.");

	}

}
