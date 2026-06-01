package com.mycrewsoft.domain.mail.service;

import com.mycrewsoft.domain.mail.dto.GoogleTokenResponse;
import com.mycrewsoft.domain.mail.dto.GoogleUserInfoResponse;

public interface GoogleOAuthClient {

    GoogleTokenResponse exchangeCode(String code);

    GoogleUserInfoResponse fetchUserInfo(String accessToken);
}
