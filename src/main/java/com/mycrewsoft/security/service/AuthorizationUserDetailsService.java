package com.mycrewsoft.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

public class AuthorizationUserDetailsService implements UserDetailsService{
	
	public AuthorizationUserDetails loadUserByUserId(Long userId) throws CustomException {
		try {
			
		} catch (UsernameNotFoundException e) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}
		
		return null;
	}

	/**
	 * 사용 금지 loadUserByUserId를 사용할 것
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
