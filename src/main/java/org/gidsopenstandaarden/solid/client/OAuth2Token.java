package org.gidsopenstandaarden.solid.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 *
 */
public class OAuth2Token implements Serializable {
	@JsonProperty("access_token")
	String accessToken;
	@JsonProperty("refresh_token")
	String refreshToken;
	@JsonProperty("id_token")
	String idToken;
	@JsonProperty("token_type")
	String tokenType;
	@JsonProperty("expires_in")
	Long expiresIn;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof OAuth2Token)) return false;

		OAuth2Token token = (OAuth2Token) o;

		return new EqualsBuilder()
				.append(accessToken, token.accessToken)
				.append(refreshToken, token.refreshToken)
				.append(idToken, token.idToken)
				.append(tokenType, token.tokenType)
				.append(expiresIn, token.expiresIn)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(accessToken)
				.append(refreshToken)
				.append(idToken)
				.append(tokenType)
				.append(expiresIn)
				.toHashCode();
	}
}
