package com.mycrewsoft.domain.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.gmail.GmailSyncResult;
import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

import reactor.core.publisher.Mono;

class WebClientGoogleGmailClientTest {

    @Test
    void syncMessagesFallsBackToInitialSyncWhenHistoryIdIsInvalid() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> respond(request, calledUris));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailMapper.class));

        GmailSyncResult result = client.syncMessages(account(), "expired-history-id", 10);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().getFirst().getExternalMessageId()).isEqualTo("msg-1");
        assertThat(result.getLatestHistoryId()).isEqualTo("history-message");
        assertThat(calledUris).anyMatch(uri -> uri.contains("/history"));
        assertThat(calledUris).anyMatch(uri -> uri.contains("/messages?maxResults=10"));
        assertThat(calledUris).anyMatch(uri -> uri.contains("/messages/msg-1?format=full"));
    }

    private Mono<ClientResponse> respond(ClientRequest request, List<String> calledUris) {
        String uri = request.url().toString();
        calledUris.add(uri);
        if (uri.contains("/history")) {
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{}")
                    .build());
        }
        if (uri.contains("/messages?maxResults=10")) {
            return Mono.just(json(HttpStatus.OK, """
                    {
                      "messages": [
                        { "id": "msg-1", "threadId": "thread-1" }
                      ],
                      "historyId": "history-list"
                    }
                    """));
        }
        if (uri.contains("/messages/msg-1?format=full")) {
            return Mono.just(json(HttpStatus.OK, """
                    {
                      "id": "msg-1",
                      "threadId": "thread-1",
                      "historyId": "history-message",
                      "snippet": "hello",
                      "internalDate": "1704067200000",
                      "labelIds": ["INBOX", "UNREAD"],
                      "payload": {
                        "mimeType": "text/plain",
                        "headers": [
                          { "name": "Subject", "value": "Hello" },
                          { "name": "From", "value": "sender@example.com" },
                          { "name": "To", "value": "user@example.com" },
                          { "name": "Date", "value": "Mon, 01 Jan 2024 00:00:00 +0000" }
                        ],
                        "body": {}
                      }
                    }
                    """));
        }
        return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private ClientResponse json(HttpStatus status, String body) {
        return ClientResponse.create(status)
                .header("Content-Type", "application/json")
                .body(body)
                .build();
    }

    private MailAccountVO account() {
        MailAccountVO account = new MailAccountVO();
        account.setEmpId(1L);
        account.setEmailAddr("user@example.com");
        account.setAccessToken("access-token");
        account.setScopeCn("https://www.googleapis.com/auth/gmail.readonly");
        account.setTokenExprDt(LocalDateTime.now().plusHours(1));
        return account;
    }
}
