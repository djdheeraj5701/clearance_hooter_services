package com.clearance_hooter.clearance_hooter_services.features.users;

import com.clearance_hooter.clearance_hooter_services.dto.User;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }


    @GetMapping("/{userId}")
    public Optional<User> getUser(@PathVariable long userId) {
        return userService.getUser(userId);
    }
}