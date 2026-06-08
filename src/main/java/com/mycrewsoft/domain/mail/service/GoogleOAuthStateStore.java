package com.mycrewsoft.domain.mail.service;

public interface GoogleOAuthStateStore {

    default void save(String state, Long empId) {
        save(state, empId, null);
    }

    void save(String state, Long empId, String context);

    GoogleOAuthState consume(String state);
}
