package com.treading_backend.service;

import com.treading_backend.domain.VerificationType;
import com.treading_backend.model.ForgotPasswordToken;
import com.treading_backend.model.User;
import com.treading_backend.repository.ForgotPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

    /**
     * 👉 This service handles all the core logic related to the "Forgot Password" functionality.
     * ✅ It creates, retrieves, verifies, and deletes password reset tokens (OTP-based).
     *
     * What this service does:
     * 🔐 createToken(...) - Generates a new ForgotPasswordToken for a user (with OTP, ID, etc.) and saves it.
     * 🔍 findById(...) - Retrieves a token from the database by its unique ID.
     * 🧑 findByUser(...) - Retrieves a token using the user's ID (used for checking if the user has a valid token).
     * ❌ deleteToken(...) - Deletes the token from the DB after successful verification or expiration.
     * ✔️ verifyToken(...) - Compares the OTP entered by the user with the one stored in the token to validate.
     *
     * 📦 Uses ForgotPasswordRepository to persist and fetch tokens from the database.
     * 💡 This service ensures OTP verification logic is handled cleanly and securely.
     */

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService{
    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;



    @Override
    public ForgotPasswordToken createToken(User user,
                                           String id,
                                           String otp,
                                           VerificationType verificationType,
                                           String sendTo
    ) {
        ForgotPasswordToken forgotPasswordToken=new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        forgotPasswordToken.setId(id);
        forgotPasswordToken.setOtp(otp);
        forgotPasswordToken.setVerificationType(verificationType);
        forgotPasswordToken.setSendTo(sendTo);

        return forgotPasswordRepository.save(forgotPasswordToken);
    }

    @Override
    public ForgotPasswordToken findById(String id) {
        Optional<ForgotPasswordToken> opt=forgotPasswordRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public ForgotPasswordToken findByUser(Long userId) {
        return forgotPasswordRepository.findByUserId(userId);
    }

    @Override
    public void deleteToken(ForgotPasswordToken token) {

        forgotPasswordRepository.delete(token);

    }

    @Override
    public boolean verifyToken(ForgotPasswordToken token, String otp) {
        return token.getOtp().equals(otp);
    }
}
