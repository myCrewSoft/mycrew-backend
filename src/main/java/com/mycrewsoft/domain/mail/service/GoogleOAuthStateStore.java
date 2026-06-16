package com.mycrewsoft.domain.mail.service;

public interface GoogleOAuthStateStore {

    default void save(String state, Long empId) {
        save(state, empId, null);
    }

    default void save(String state, Long empId, String context) {
        save(state, empId, context, null);
    }

    void save(String state, Long empId, String context, String emailAddr);

    GoogleOAuthState consume(String state);
}
