package com.mycrewsoft.security.users;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationUserDetails implements UserDetails{
	private Long mbrId;
	private String username;
	private String password;
	private boolean enabled;
	private Integer authVersion;
	private Collection<? extends GrantedAuthority> authorities;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return String.valueOf(mbrId);
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
