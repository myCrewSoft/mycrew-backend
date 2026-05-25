package com.mycrewsoft.security.users;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mycrewsoft.security.authz.ScopedPermission;

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
	private List<ScopedPermission> scopedPermissions = new ArrayList<>();

	public AuthorizationUserDetails(
			Long mbrId,
			String username,
			String password,
			boolean enabled,
			Integer authVersion,
			Collection<? extends GrantedAuthority> authorities) {
		this.mbrId = mbrId;
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.authVersion = authVersion;
		this.authorities = authorities;
	}
	
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
