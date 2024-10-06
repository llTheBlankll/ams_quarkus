package com.pshs.ams.services;

import com.pshs.ams.models.dto.custom.JWTInformationDTO;
import com.pshs.ams.models.dto.user.LoginRequest;
import com.pshs.ams.models.entities.User;

import java.util.Optional;

public interface UserService {

	JWTInformationDTO login(LoginRequest loginRequest);
	Optional<User> getUserByUsername(String username);
}