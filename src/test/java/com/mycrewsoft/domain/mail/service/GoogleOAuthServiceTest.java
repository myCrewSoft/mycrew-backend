package com.mycrewsoft.domain.mail.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.dto.response.GoogleOAuthAuthorizeResponse;
import com.mycrewsoft.domain.mail.dto.response.GoogleTokenResponse;
import com.mycrewsoft.domain.mail.dto.response.GoogleUserInfoResponse;
import com.mycrewsoft.domain.mail.mapper.MailAccountMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

class GoogleOAuthServiceTest {

    @Test
    void createAuthorizationUrlStoresStateForCurrentEmployee() {
        FakeStateStore stateStore = new FakeStateStore();
        FakeMailAccountMapper mapper = new FakeMailAccountMapper();
        GoogleOAuthService service = newService(stateStore, mapper, new FakeGoogleOAuthClient());

        GoogleOAuthAuthorizeResponse response = service.createAuthorizationUrl(1001L, null);

        assertThat(stateStore.savedEmpId).isEqualTo(1001L);
        assertThat(stateStore.savedContext).isNull();
        assertThat(response.getAuthorizationUrl())
                .contains("https://accounts.google.com/o/oauth2/v2/auth")
                .contains("client_id=test-client")
                .contains("redirect_uri=http://localhost/api/mail/oauth/google/callback")
                .contains("access_type=offline")
                .contains("prompt=consent")
                .contains("state=" + stateStore.savedState);
    }

    @Test
    void callbackExchangesCodeAndStoresGoogleMailAccount() {
        FakeStateStore stateStore = new FakeStateStore();
        stateStore.save("state-1", 1001L, null);
        FakeMailAccountMapper mapper = new FakeMailAccountMapper();
        FakeGoogleOAuthClient client = new FakeGoogleOAuthClient();
        GoogleOAuthService service = newService(stateStore, mapper, client);

        String redirectUri = service.handleCallback("auth-code", "state-1");

        assertThat(client.exchangedCode).isEqualTo("auth-code");
        assertThat(mapper.saved.getEmpId()).isEqualTo(1001L);
        assertThat(mapper.saved.getProviderCd()).isEqualTo("GOOGLE");
        assertThat(mapper.saved.getEmailAddr()).isEqualTo("person@gmail.com");
        assertThat(mapper.saved.getGoogleSubId()).isEqualTo("google-sub-1");
        assertThat(mapper.saved.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(mapper.saved.getTokenStatusCd()).isEqualTo("ACTIVE");
        assertThat(mapper.saved.getUseYn()).isEqualTo("Y");
        assertThat(redirectUri).isEqualTo("http://localhost:5173/first-login?mailConnected=Y");
    }

    @Test
    void callbackRedirectsToMailWhenAuthorizationStartedFromMailContext() {
        FakeStateStore stateStore = new FakeStateStore();
        FakeMailAccountMapper mapper = new FakeMailAccountMapper();
        GoogleOAuthService service = newService(stateStore, mapper, new FakeGoogleOAuthClient());

        service.createAuthorizationUrl(1001L, "mail");
        String redirectUri = service.handleCallback("auth-code", stateStore.savedState);

        assertThat(stateStore.savedContext).isEqualTo("mail");
        assertThat(redirectUri).isEqualTo("http://localhost:5173/mail");
    }

    private GoogleOAuthService newService(
            GoogleOAuthStateStore stateStore,
            MailAccountMapper mapper,
            GoogleOAuthClient client) {
        GoogleOAuthProperties properties = new GoogleOAuthProperties();
        properties.setClientId("test-client");
        properties.setClientSecret("test-secret");
        properties.setRedirectUri("http://localhost/api/mail/oauth/google/callback");
        properties.setFrontendSuccessUri("http://localhost:5173/first-login?mailConnected=Y");
        properties.setFrontendFailureUri("http://localhost:5173/first-login?mailConnected=N");
        properties.setFrontendMailSuccessUri("http://localhost:5173/mail");
        properties.setScopes(List.of(
                "openid",
                "email",
                "profile",
                "https://www.googleapis.com/auth/gmail.readonly",
                "https://www.googleapis.com/auth/gmail.send"));
        return new GoogleOAuthService(properties, stateStore, client, mapper);
    }

    private static class FakeStateStore implements GoogleOAuthStateStore {
        private String savedState;
        private Long savedEmpId;
        private String savedContext;

        @Override
        public void save(String state, Long empId) {
            save(state, empId, null);
        }

        @Override
        public void save(String state, Long empId, String context) {
            this.savedState = state;
            this.savedEmpId = empId;
            this.savedContext = context;
        }

        @Override
        public GoogleOAuthState consume(String state) {
            return new GoogleOAuthState(savedEmpId, savedContext);
        }
    }

    private static class FakeGoogleOAuthClient implements GoogleOAuthClient {
        private String exchangedCode;

        @Override
        public GoogleTokenResponse exchangeCode(String code) {
            this.exchangedCode = code;
            GoogleTokenResponse response = new GoogleTokenResponse();
            response.setAccessToken("access-token");
            response.setRefreshToken("refresh-token");
            response.setExpiresIn(3600L);
            response.setScope("openid email profile https://www.googleapis.com/auth/gmail.readonly");
            return response;
        }

        @Override
        public GoogleUserInfoResponse fetchUserInfo(String accessToken) {
            GoogleUserInfoResponse response = new GoogleUserInfoResponse();
            response.setSub("google-sub-1");
            response.setEmail("person@gmail.com");
            return response;
        }
    }

    private static class FakeMailAccountMapper implements MailAccountMapper {
        private MailAccountVO saved;

        @Override
        public int upsertGoogleMailAccount(MailAccountVO mailAccount) {
            this.saved = mailAccount;
            return 1;
        }

        @Override
        public int existsActiveMailAccount(Long empId) {
            return 0;
        }

        @Override
        public int existsActiveGoogleMailAccountByEmail(Long empId, String emailAddr) {
            return 0;
        }
    }
}
