package com.pshs.ams.app.users.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class LoginRequest {
	private String username;
	private String password;
}
