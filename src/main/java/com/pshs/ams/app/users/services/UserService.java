package com.pshs.ams.app.users.services;

import com.pshs.ams.global.models.custom.JWTInformation;
import com.pshs.ams.app.users.models.dto.LoginRequest;
import com.pshs.ams.app.users.models.entities.User;

import java.util.Optional;

public interface UserService {

	JWTInformation login(LoginRequest loginRequest);
	Optional<User> getUserByUsername(String username);
}
