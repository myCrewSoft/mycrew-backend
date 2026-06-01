package com.mycrewsoft.domain.mail.service;

public interface GoogleOAuthStateStore {

    void save(String state, Long empId);

    Long consume(String state);
}
