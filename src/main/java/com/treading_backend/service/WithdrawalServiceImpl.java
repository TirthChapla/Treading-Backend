package com.treading_backend.service;

import com.treading_backend.domain.WithdrawalStatus;
import com.treading_backend.model.User;
import com.treading_backend.model.Withdrawal;
import com.treading_backend.repository.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    // ✅ New withdrawal request banavvani logic
    @Override
    public Withdrawal requestWithdrawal(Long amount, User user) {
        Withdrawal withdrawal = new Withdrawal();

        withdrawal.setAmount(amount); // 👉 ketla paisa withdraw karva che
        withdrawal.setStatus(WithdrawalStatus.PENDING); // 👉 default status = pending
        withdrawal.setDate(LocalDateTime.now()); // 👉 current date/time set kari
        withdrawal.setUser(user); // 👉 user attach kari lidho

        return withdrawalRepository.save(withdrawal); // ❤️ DB ma save kari didho
    }

    // ✅ Accept or Decline withdrawal request
    @Override
    public Withdrawal procedWithdrawal(Long withdrawalId, boolean accept) throws Exception {
        Optional<Withdrawal> withdrawalOptional = withdrawalRepository.findById(withdrawalId);

        if (withdrawalOptional.isEmpty()) {
            // 👉 wrong ID aapi che
            throw new Exception("withdrawal id is wrong...");
        }

        Withdrawal withdrawal = withdrawalOptional.get();

        withdrawal.setDate(LocalDateTime.now()); // 👉 process thaya time update karvo jaruri che

        if (accept) {
            withdrawal.setStatus(WithdrawalStatus.SUCCESS); // ✅ approve kari didho
        } else {
            withdrawal.setStatus(WithdrawalStatus.DECLINE); // ❌ decline kari didho
        }

        return withdrawalRepository.save(withdrawal); // ❤️ update kari save
    }

    // ✅ ek user ni badhi withdrawal history lavvani
    @Override
    public List<Withdrawal> getUsersWithdrawalHistory(User user) {
        return withdrawalRepository.findByUserId(user.getId()); // 👉 user ID thi filter
    }

    // ✅ admin ne badhi request joi shake
    @Override
    public List<Withdrawal> getAllWithdrawalRequest() {
        return withdrawalRepository.findAll(); // ❤️ fetch all withdrawals
    }
}
