package com.mycrewsoft.domain.mail.service;

import com.mycrewsoft.domain.mail.dto.response.GoogleTokenResponse;
import com.mycrewsoft.domain.mail.dto.response.GoogleUserInfoResponse;

public interface GoogleOAuthClient {

    GoogleTokenResponse exchangeCode(String code);

    GoogleUserInfoResponse fetchUserInfo(String accessToken);
}
