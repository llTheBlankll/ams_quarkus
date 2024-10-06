package com.pshs.ams.models.dto.custom;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.DefaultJWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class JWTInformationDTO {
	private String rawToken;
	private Set<String> audience;
	private Set<String> groups;
	private String name;
	private List<Map<String, String>> claims = new ArrayList<>();
	private String issuer;
	private String subject;
	private Long issuedAtTime;
	private Long expirationTime;

	public JWTInformationDTO(DefaultJWTCallerPrincipal principal) {
		this.name = principal.getName();
		this.issuer = principal.getIssuer();
		principal.getClaimNames().forEach(name -> {
			Object claimValue = principal.getClaim(name);
			this.claims.add(
				Map.of("name", name, "value", claimValue != null ? claimValue.toString() : "null")
			);
		});
		this.rawToken = principal.getRawToken();
		this.audience = principal.getAudience();
		this.groups = principal.getGroups();
		this.subject = principal.getSubject();
		this.issuedAtTime = principal.getIssuedAtTime();
		this.expirationTime = principal.getExpirationTime();
	}

	public JWTInformationDTO(String token) throws ParseException {
		DefaultJWTParser parser = new DefaultJWTParser();
		JsonWebToken jwtToken = parser.parseOnly(token);
		this.name = jwtToken.getName();
		this.issuer = jwtToken.getIssuer();
		jwtToken.getClaimNames().forEach(name -> {
			Object claimValue = jwtToken.getClaim(name);
			this.claims.add(
				Map.of("name", name, "value", claimValue != null ? claimValue.toString() : "null")
			);
		});
		this.rawToken = jwtToken.getRawToken();
		this.audience = jwtToken.getAudience();
		this.groups = jwtToken.getGroups();
		this.subject = jwtToken.getSubject();
		this.issuedAtTime = jwtToken.getIssuedAtTime();
		this.expirationTime = jwtToken.getExpirationTime();
	}
}