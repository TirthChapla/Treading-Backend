package com.treading_backend.service;

import com.treading_backend.model.TwoFactorOTP;
import com.treading_backend.model.User;
import com.treading_backend.repository.TwoFactorOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;





@Service
public class TwoFactorOtpServiceImpl implements TwoFactorOtpService {

    @Autowired
    private TwoFactorOtpRepository twoFactorOtpRepository;

    /// ✅ Create 2FA OTP Entry
    ///👉 1. Generate a unique UUID string as ID.
    ///👉 2. Create a new TwoFactorOTP object and set the required fields: ID, user, OTP, JWT.
    ///👉 3. Save the OTP object into the database using repository.
    ///👉 4. Return the saved object.
    @Override
    public TwoFactorOTP createTwoFactorOtp(User user, String otp, String jwt) {
        UUID uuid = UUID.randomUUID();                  // Generate unique ID
        String id = uuid.toString();

        TwoFactorOTP twoFactorOTP = new TwoFactorOTP();
        twoFactorOTP.setId(id);
        twoFactorOTP.setUser(user);
        twoFactorOTP.setOtp(otp);
        twoFactorOTP.setJwt(jwt);

        return twoFactorOtpRepository.save(twoFactorOTP);  // Save to DB
    }


    /// ✅ Get OTP record by User ID
    ///👉 Fetch the latest OTP record associated with the given user ID.
    @Override
    public TwoFactorOTP findByUser(Long userId) {
        return twoFactorOtpRepository.findByUserId(userId);
    }

    /// ✅ Get OTP record by OTP ID
    ///👉 Use the ID to retrieve the OTP object from the database.
    ///👉 Return the object or null if not found.
    @Override
    public TwoFactorOTP findById(String id) {
        Optional<TwoFactorOTP> twoFactorOtp = twoFactorOtpRepository.findById(id);
        return twoFactorOtp.orElse(null);
    }

    /// ✅ Verify OTP
    ///👉 Check if the OTP entered by the user matches the one stored in DB.
    ///👉 Return true if matched, else false.
    @Override
    public boolean verifyTwoFactorOtp(TwoFactorOTP twoFactorOtp, String otp) {
        return twoFactorOtp.getOtp().equals(otp);
    }

    /// ✅ Delete OTP
    ///👉 Remove the OTP record from DB once verified or expired.
    @Override
    public void deleteTwoFactorOtp(TwoFactorOTP twoFactorOTP) {
        twoFactorOtpRepository.delete(twoFactorOTP);
    }
}