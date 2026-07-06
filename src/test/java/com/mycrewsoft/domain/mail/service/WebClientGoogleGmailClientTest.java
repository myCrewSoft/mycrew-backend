package com.mycrewsoft.domain.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.gmail.GmailSyncResult;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

import reactor.core.publisher.Mono;

@ExtendWith(OutputCaptureExtension.class)
class WebClientGoogleGmailClientTest {

    @Test
    void syncMessagesFallsBackToInitialSyncWhenHistoryIdIsInvalid() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> respond(request, calledUris));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        GmailSyncResult result = client.syncMessages(account(), "expired-history-id", 10);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().getFirst().getExternalMessageId()).isEqualTo("msg-1");
        assertThat(result.getLatestHistoryId()).isEqualTo("history-message");
        assertThat(calledUris).anyMatch(uri -> uri.contains("/history"));
        assertThat(calledUris).anyMatch(uri -> uri.contains("/messages?maxResults=10"));
        assertThat(calledUris).anyMatch(uri -> uri.contains("/messages/msg-1?format=full"));
    }

    @Test
    void historySyncReturnsLabelChangesWithoutFetchingFullMessageForLabelOnlyChanges() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> respondWithMessageAddedAndLabelChange(request, calledUris));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        GmailSyncResult result = client.syncMessages(account(), "history-1", 10);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().getFirst().getExternalMessageId()).isEqualTo("msg-new");
        assertThat(result.getLabelChanges()).hasSize(1);
        assertThat(result.getLabelChanges().getFirst().getExternalMessageId()).isEqualTo("msg-old");
        assertThat(result.getLabelChanges().getFirst().getAddedLabels()).containsExactly("UNREAD", "IMPORTANT");
        assertThat(result.getLabelChanges().getFirst().getRemovedLabels()).containsExactly("TRASH");
        assertThat(calledUris).anyMatch(uri -> uri.contains("/history"));
        assertThat(calledUris).anyMatch(uri -> uri.contains("/messages/msg-new?format=full"));
        assertThat(calledUris).noneMatch(uri -> uri.contains("/messages/msg-old?format=full"));
    }

    @Test
    void initialSyncFetchesFullMessagesConcurrently() {
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger maxInFlight = new AtomicInteger();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> respondWithDelayedMessages(request, inFlight, maxInFlight));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        GmailSyncResult result = client.syncMessages(account(), null, 3);

        assertThat(result.getMessages()).hasSize(3);
        assertThat(maxInFlight.get()).isGreaterThan(1);
    }

    @Test
    void historySyncFetchesGenericMessagesWhenTypedEntriesAreMissing() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> respondWithGenericHistoryMessage(request, calledUris));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        GmailSyncResult result = client.syncMessages(account(), "history-1", 10);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().getFirst().getExternalMessageId()).isEqualTo("msg-generic");
        assertThat(result.getLabelChanges()).isEmpty();
        assertThat(calledUris).anyMatch(uri -> uri.contains("/messages/msg-generic?format=full"));
    }

    @Test
    void syncMessagesLogsWebClientResponseDetailsWhenGmailApiFails(CapturedOutput output) {
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> Mono.just(json(HttpStatus.FORBIDDEN, """
                        {
                          "error": {
                            "code": 403,
                            "message": "Gmail API has not been used in project",
                            "status": "PERMISSION_DENIED"
                          }
                        }
                        """)));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        assertThatThrownBy(() -> client.syncMessages(account(), null, 10))
                .isInstanceOf(CustomException.class);

        assertThat(output).contains("Gmail API request failed");
        assertThat(output).contains("phase=sync");
        assertThat(output).contains("status=403 FORBIDDEN");
        assertThat(output).contains("Gmail API has not been used in project");
    }

    @Test
    void deleteMessageLogsWebClientResponseDetailsWhenGmailApiFails(CapturedOutput output) {
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> Mono.just(json(HttpStatus.NOT_FOUND, """
                        {
                          "error": {
                            "code": 404,
                            "message": "Requested entity was not found.",
                            "status": "NOT_FOUND"
                          }
                        }
                        """)));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        assertThatThrownBy(() -> client.deleteMessage(account(), "missing-message"))
                .isInstanceOf(CustomException.class);

        assertThat(output).contains("Gmail API request failed");
        assertThat(output).contains("phase=delete-message");
        assertThat(output).contains("status=404 NOT_FOUND");
        assertThat(output).contains("Requested entity was not found.");
    }

    @Test
    void syncMessagesDownloadsAttachmentLargerThanDefaultWebClientBuffer() {
        byte[] attachmentContent = new byte[300_000];
        String attachmentData = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(attachmentContent);
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> respondWithLargeAttachment(request, attachmentData));
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        GmailSyncResult result = client.syncMessages(account(), null, 10);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().getFirst().getAttachments()).hasSize(1);
        assertThat(result.getMessages().getFirst().getAttachments().getFirst().getContent()).hasSize(300_000);
    }

    @Test
    void refreshTokenClientErrorMarksAccountInvalid() {
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> Mono.just(json(HttpStatus.BAD_REQUEST, """
                        {
                          "error": "invalid_grant"
                        }
                        """)));
        MailAccountTokenService tokenService = mock(MailAccountTokenService.class);
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                tokenService);
        MailAccountVO account = account();
        account.setRefreshToken("refresh-token");
        account.setTokenExprDt(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> client.syncMessages(account, null, 10))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MAIL_TOKEN_INVALID);

        verify(tokenService).markInvalid(account);
    }

    @Test
    void markReadUsesGmailBatchModifyEndpointForMultipleMessages() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> {
                    calledUris.add(request.url().toString());
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                });
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        client.markRead(account(), List.of("msg-1", "msg-2"));

        assertThat(calledUris).containsExactly(
                "https://gmail.googleapis.com/gmail/v1/users/me/messages/batchModify");
    }

    @Test
    void updateImportantUsesGmailBatchModifyEndpointForMultipleMessages() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> {
                    calledUris.add(request.url().toString());
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                });
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        client.updateImportant(account(), List.of("msg-1", "msg-2"), true);

        assertThat(calledUris).containsExactly(
                "https://gmail.googleapis.com/gmail/v1/users/me/messages/batchModify");
    }

    @Test
    void trashMessagesUsesGmailBatchModifyEndpoint() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> {
                    calledUris.add(request.url().toString());
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                });
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        client.trashMessages(account(), List.of("msg-1", "msg-2"));

        assertThat(calledUris).containsExactly(
                "https://gmail.googleapis.com/gmail/v1/users/me/messages/batchModify");
    }

    @Test
    void deleteMessagesUsesGmailBatchDeleteEndpoint() {
        List<String> calledUris = new ArrayList<>();
        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(request -> {
                    calledUris.add(request.url().toString());
                    return Mono.just(ClientResponse.create(HttpStatus.OK).build());
                });
        WebClientGoogleGmailClient client = new WebClientGoogleGmailClient(
                builder,
                new GoogleOAuthProperties(),
                mock(MailAccountTokenService.class));

        client.deleteMessages(account(), List.of("msg-1", "msg-2"));

        assertThat(calledUris).containsExactly(
                "https://gmail.googleapis.com/gmail/v1/users/me/messages/batchDelete");
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

    private Mono<ClientResponse> respondWithMessageAddedAndLabelChange(ClientRequest request, List<String> calledUris) {
        String uri = request.url().toString();
        calledUris.add(uri);
        if (uri.contains("/history")) {
            return Mono.just(json(HttpStatus.OK, """
                    {
                      "history": [
                        {
                          "messages": [
                            { "id": "msg-old", "threadId": "thread-old" }
                          ],
                          "messagesAdded": [
                            { "message": { "id": "msg-new", "threadId": "thread-new" } }
                          ],
                          "labelsAdded": [
                            {
                              "message": { "id": "msg-old", "threadId": "thread-old" },
                              "labelIds": ["UNREAD", "IMPORTANT"]
                            }
                          ],
                          "labelsRemoved": [
                            {
                              "message": { "id": "msg-old", "threadId": "thread-old" },
                              "labelIds": ["TRASH"]
                            }
                          ]
                        }
                      ],
                      "historyId": "history-2"
                    }
                    """));
        }
        if (uri.contains("/messages/msg-new?format=full")) {
            return Mono.just(messageResponse("msg-new", "thread-new"));
        }
        return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private Mono<ClientResponse> respondWithDelayedMessages(ClientRequest request,
                                                            AtomicInteger inFlight,
                                                            AtomicInteger maxInFlight) {
        String uri = request.url().toString();
        if (uri.contains("/messages?maxResults=3")) {
            return Mono.just(json(HttpStatus.OK, """
                    {
                      "messages": [
                        { "id": "msg-1", "threadId": "thread-1" },
                        { "id": "msg-2", "threadId": "thread-2" },
                        { "id": "msg-3", "threadId": "thread-3" }
                      ],
                      "historyId": "history-list"
                    }
                    """));
        }
        if (uri.contains("/messages/msg-")) {
            String messageId = uri.substring(uri.indexOf("/messages/") + "/messages/".length(), uri.indexOf("?format=full"));
            return Mono.defer(() -> {
                int current = inFlight.incrementAndGet();
                maxInFlight.updateAndGet(previous -> Math.max(previous, current));
                return Mono.delay(Duration.ofMillis(100))
                        .map(ignored -> messageResponse(messageId, "thread-" + messageId))
                        .doFinally(signalType -> inFlight.decrementAndGet());
            });
        }
        return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private Mono<ClientResponse> respondWithGenericHistoryMessage(ClientRequest request, List<String> calledUris) {
        String uri = request.url().toString();
        calledUris.add(uri);
        if (uri.contains("/history")) {
            return Mono.just(json(HttpStatus.OK, """
                    {
                      "history": [
                        {
                          "messages": [
                            { "id": "msg-generic", "threadId": "thread-generic" }
                          ]
                        }
                      ],
                      "historyId": "history-2"
                    }
                    """));
        }
        if (uri.contains("/messages/msg-generic?format=full")) {
            return Mono.just(messageResponse("msg-generic", "thread-generic"));
        }
        return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private ClientResponse messageResponse(String messageId, String threadId) {
        return json(HttpStatus.OK, """
                {
                  "id": "%s",
                  "threadId": "%s",
                  "historyId": "history-message-%s",
                  "snippet": "hello",
                  "internalDate": "1704067200000",
                  "labelIds": ["INBOX"],
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
                """.formatted(messageId, threadId, messageId));
    }

    private Mono<ClientResponse> respondWithLargeAttachment(ClientRequest request, String attachmentData) {
        String uri = request.url().toString();
        if (uri.contains("/messages?maxResults=10")) {
            return Mono.just(json(HttpStatus.OK, """
                    {
                      "messages": [
                        { "id": "msg-large", "threadId": "thread-1" }
                      ],
                      "historyId": "history-list"
                    }
                    """));
        }
        if (uri.contains("/messages/msg-large?format=full")) {
            return Mono.just(json(HttpStatus.OK, """
                    {
                      "id": "msg-large",
                      "threadId": "thread-1",
                      "historyId": "history-message",
                      "snippet": "large attachment",
                      "internalDate": "1704067200000",
                      "labelIds": ["INBOX"],
                      "payload": {
                        "mimeType": "multipart/mixed",
                        "headers": [
                          { "name": "Subject", "value": "Large" },
                          { "name": "From", "value": "sender@example.com" },
                          { "name": "To", "value": "user@example.com" },
                          { "name": "Date", "value": "Mon, 01 Jan 2024 00:00:00 +0000" }
                        ],
                        "parts": [
                          {
                            "filename": "large.bin",
                            "mimeType": "application/octet-stream",
                            "body": { "attachmentId": "att-large" }
                          }
                        ]
                      }
                    }
                    """));
        }
        if (uri.contains("/attachments/att-large")) {
            return Mono.just(json(HttpStatus.OK, "{\"data\":\"" + attachmentData + "\"}"));
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
