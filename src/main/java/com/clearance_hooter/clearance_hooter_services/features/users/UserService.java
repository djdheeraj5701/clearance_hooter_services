package com.clearance_hooter.clearance_hooter_services.features.users;

import com.clearance_hooter.clearance_hooter_services.dto.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUser(long userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public boolean updateWalletAmount(long userId, long amount) {
        return userRepository.findById(userId).map(user -> {
            if (user.getWalletAmount() >= amount) {
                user.setWalletAmount(user.getWalletAmount() - amount);
                userRepository.save(user);
                return true;
            }
            return false;
        }).orElse(false);
    }
}
