package com.treading_backend.service;

import com.treading_backend.domain.VerificationType;
import com.treading_backend.model.User;
import com.treading_backend.model.VerificationCode;
import com.treading_backend.repository.VerificationRepository;
import com.treading_backend.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationServiceImpl implements VerificationService{

    @Autowired
    private VerificationRepository verificationRepository;

    ///👉 We are sending verification OTP

    @Override
    public VerificationCode sendVerificationOTP(User user, VerificationType verificationType)
    {

        VerificationCode verificationCode = new VerificationCode();

        verificationCode.setOtp(OtpUtils.generateOTP());
        verificationCode.setUser(user);
        verificationCode.setVerificationType(verificationType);

        return verificationRepository.save(verificationCode);
    }

    ///👉 we are finding verification by the id

    @Override
    public VerificationCode findVerificationById(Long id) throws Exception
    {
        Optional<VerificationCode> verificationCodeOption=verificationRepository.findById(id);
        if(verificationCodeOption.isEmpty()){
            throw new Exception("verification not found");
        }
        return verificationCodeOption.get();
    }

    ///👉 find verification code by the user
    @Override
    public VerificationCode findUsersVerification(User user) throws Exception {
        return verificationRepository.findByUserId(user.getId());
    }

    ///👉 Verify the OTP
    @Override
    public Boolean VerifyOtp(String opt, VerificationCode verificationCode) {
        return opt.equals(verificationCode.getOtp());
    }

    ///👉 Delete the verification
    @Override
    public void deleteVerification(VerificationCode verificationCode) {
        verificationRepository.delete(verificationCode);
    }


}
