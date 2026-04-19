package com.barangay.barangay.scheduler;

import com.barangay.barangay.admin_management.model.User;
import com.barangay.barangay.admin_management.repository.Root_AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountUnlockScheduler {

    private final Root_AdminRepository userRepository;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoUnlockUsers() {

        List<User> expiredLocks = userRepository.findAllByIsLockedTrueAndLockUntilBefore(LocalDateTime.now());

        for (User user : expiredLocks) {
            user.setIsLocked(false);
            user.setLockUntil(null);
            user.setFailedAttempts(0);
            userRepository.save(user);
            System.out.println("Automatic Unlocked: " + user.getUsername()); // Pang-debug mo Jer [cite: 2025-11-23]
        }
    }
}