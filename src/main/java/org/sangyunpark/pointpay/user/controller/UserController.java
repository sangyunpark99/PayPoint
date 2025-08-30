package org.sangyunpark.pointpay.user.controller;

import jakarta.validation.Valid;
import org.sangyunpark.pointpay.user.dto.request.UserCreateRequest;
import org.sangyunpark.pointpay.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public void register(@Valid @RequestBody final UserCreateRequest request) {
        userService.createUser(request.getUsername(), request.getPointBalance());
    }
}
