package com.pshs.ams.services.impl;

import com.pshs.ams.models.dto.custom.JWTInformationDTO;
import com.pshs.ams.models.dto.user.LoginRequest;
import com.pshs.ams.models.entities.User;
import com.pshs.ams.services.UserService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserServiceImpl implements UserService {

	@Inject
	Logger logger;

	/**
	 * @param loginRequest
	 * @return
	 */
	@Override
	public JWTInformationDTO login(LoginRequest loginRequest) {
		if (loginRequest.getUsername().isEmpty() || loginRequest.getPassword().isEmpty()) {
			return null;
		}

		// Check if username exist
		Optional<User> userOptional = User.find("username", loginRequest.getUsername()).firstResultOptional();
		if (userOptional.isEmpty()) {
			logger.debug("User not found: " + loginRequest.getUsername());
			return null;
		}

		// Check if password is correct using BCrypt
		User user = userOptional.get();
		boolean isCorrect = BcryptUtil.matches(loginRequest.getPassword(), user.getPassword());
		if (!isCorrect) {
			logger.debug("Incorrect password for user: " + user.getUsername());
			return null;
		}

		// * Generate JWT token
		try {
			return new JWTInformationDTO(Jwt.issuer("http://localhost:8080")
				.upn(user.getUsername())
				.groups(new HashSet<>(List.of(user.getRole().name())))
				.claim("role", user.getRole().name())
				.claim("email", user.getEmail())
				.expiresIn(Duration.of(30, ChronoUnit.DAYS))
				.issuedAt(System.currentTimeMillis())
				.subject(user.getId().toString())
				.sign());
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}

		return null;
	}

	/**
	 * @param username
	 * @return
	 */
	@Override
	public Optional<User> getUserByUsername(String username) {
		return User.find("username", username).firstResultOptional();
	}
}