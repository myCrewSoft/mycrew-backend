package com.mycrewsoft.domain.member.service;

public interface MemberService {
     void signUp(Long empId, String password, String name);
	 String login(Long empId, String password);
}
