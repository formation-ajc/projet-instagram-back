package com.projet.instagram.service.user;

import com.projet.instagram.model.User;
import com.projet.instagram.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User update(User user) {
        return userRepository.save(user);
    }
}
