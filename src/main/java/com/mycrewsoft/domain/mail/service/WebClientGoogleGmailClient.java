package com.mycrewsoft.domain.mail.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.dto.response.GoogleTokenResponse;
import com.mycrewsoft.domain.mail.gmail.GmailMessageContent;
import com.mycrewsoft.domain.mail.gmail.GmailSendCommand;
import com.mycrewsoft.domain.mail.gmail.GmailSendResult;
import com.mycrewsoft.domain.mail.gmail.GmailSyncResult;
import com.mycrewsoft.domain.mail.gmail.GmailSyncedAttachment;
import com.mycrewsoft.domain.mail.gmail.GmailSyncedMessage;
import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientGoogleGmailClient implements GoogleGmailClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String GMAIL_USER_API = "https://gmail.googleapis.com/gmail/v1/users/me";
    private static final String GMAIL_API = GMAIL_USER_API + "/messages";
    private static final int GMAIL_MAX_IN_MEMORY_SIZE = 50 * 1024 * 1024;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WebClient.Builder webClientBuilder;
    private final GoogleOAuthProperties properties;
    private final MailMapper mailMapper;

    @Override
    public GmailSendResult sendMessage(MailAccountVO account, GmailSendCommand command) {
        try {
            String raw = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(buildMimeMessage(command).getBytes(StandardCharsets.UTF_8));

            Map<?, ?> response = webClient()
                    .post()
                    .uri(GMAIL_API + "/send")
                    .headers(headers -> headers.setBearerAuth(accessToken(account)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("raw", raw))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("id") == null) {
                throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
            }
            return new GmailSendResult(
                    String.valueOf(response.get("id")),
                    response.get("threadId") == null ? null : String.valueOf(response.get("threadId")),
                    LocalDateTime.now());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    @Override
    public GmailMessageContent getMessage(MailAccountVO account, String externalMessageId) {
        try {
            Map<?, ?> response = webClient()
                    .get()
                    .uri(GMAIL_API + "/" + externalMessageId + "?format=full")
                    .headers(headers -> headers.setBearerAuth(accessToken(account)))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
            }
            String snippet = value(response.get("snippet"));
            String content = extractBody(response);
            return new GmailMessageContent(content, snippet);
        } catch (CustomException e) {
            throw e;
        } catch (WebClientResponseException e) {
            logGmailApiFailure("get-message", e);
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    @Override
    public void markRead(MailAccountVO account, String externalMessageId) {
        modifyLabels(account, externalMessageId, List.of(), List.of("UNREAD"));
    }

    @Override
    public void updateImportant(MailAccountVO account, String externalMessageId, boolean important) {
        if (important) {
            modifyLabels(account, externalMessageId, List.of("IMPORTANT"), List.of());
        } else {
            modifyLabels(account, externalMessageId, List.of(), List.of("IMPORTANT"));
        }
    }

    @Override
    public void trashMessage(MailAccountVO account, String externalMessageId) {
        postWithoutBody(account, GMAIL_API + "/" + externalMessageId + "/trash");
    }

    @Override
    public void untrashMessage(MailAccountVO account, String externalMessageId) {
        postWithoutBody(account, GMAIL_API + "/" + externalMessageId + "/untrash");
    }

    @Override
    public void deleteMessage(MailAccountVO account, String externalMessageId) {
        try {
            webClient()
                    .delete()
                    .uri(GMAIL_API + "/" + externalMessageId)
                    .headers(headers -> headers.setBearerAuth(accessToken(account)))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (CustomException e) {
            throw e;
        } catch (WebClientResponseException e) {
            logGmailApiFailure("delete-message", e);
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    @Override
    public GmailSyncResult syncMessages(MailAccountVO account, String startHistoryId, int maxResults) {
        try {
            if (startHistoryId == null || startHistoryId.isBlank()) {
                return initialSync(account, maxResults);
            }
            return historySync(account, startHistoryId, maxResults);
        } catch (WebClientResponseException.NotFound e) {
            try {
                return initialSync(account, maxResults);
            } catch (WebClientResponseException fallbackException) {
                logGmailApiFailure("initial-sync-after-history-not-found", fallbackException);
                throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
            }
        } catch (CustomException e) {
            throw e;
        } catch (WebClientResponseException e) {
            logGmailApiFailure("sync", e);
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    private void logGmailApiFailure(String phase, WebClientResponseException e) {
        String uri = e.getRequest() == null ? "unknown" : e.getRequest().getURI().toString();
        log.error(
                "Gmail API request failed. phase={}, status={}, uri={}, responseBody={}",
                phase,
                e.getStatusCode(),
                uri,
                abbreviate(e.getResponseBodyAsString()),
                e);
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() > 1000 ? normalized.substring(0, 1000) + "..." : normalized;
    }

    private WebClient webClient() {
        return webClientBuilder.clone()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(GMAIL_MAX_IN_MEMORY_SIZE))
                .build();
    }

    @SuppressWarnings("unchecked")
    private GmailSyncResult initialSync(MailAccountVO account, int maxResults) {
        Map<?, ?> response = webClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("gmail.googleapis.com")
                        .path("/gmail/v1/users/me/messages")
                        .queryParam("maxResults", maxResults)
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken(account)))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Set<String> messageIds = new LinkedHashSet<>();
        if (response != null && response.get("messages") instanceof List<?> messages) {
            for (Object message : messages) {
                if (message instanceof Map<?, ?> messageMap && messageMap.get("id") != null) {
                    messageIds.add(String.valueOf(messageMap.get("id")));
                }
            }
        }
        return fetchMessages(account, messageIds, value(response == null ? null : response.get("historyId")));
    }

    @SuppressWarnings("unchecked")
    private GmailSyncResult historySync(MailAccountVO account, String startHistoryId, int maxResults) {
        Map<?, ?> response = webClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("gmail.googleapis.com")
                        .path("/gmail/v1/users/me/history")
                        .queryParam("startHistoryId", startHistoryId)
                        .queryParam("maxResults", maxResults)
                        .queryParam("historyTypes", "messageAdded")
                        .queryParam("historyTypes", "labelAdded")
                        .queryParam("historyTypes", "labelRemoved")
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken(account)))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Set<String> messageIds = new LinkedHashSet<>();
        if (response != null && response.get("history") instanceof List<?> historyList) {
            for (Object history : historyList) {
                if (history instanceof Map<?, ?> historyMap) {
                    collectHistoryMessageIds(historyMap, "messages", messageIds);
                    collectHistoryMessageIds(historyMap, "messagesAdded", messageIds);
                    collectHistoryMessageIds(historyMap, "labelsAdded", messageIds);
                    collectHistoryMessageIds(historyMap, "labelsRemoved", messageIds);
                }
            }
        }
        return fetchMessages(account, limitIds(messageIds, maxResults), value(response == null ? null : response.get("historyId")));
    }

    private Set<String> limitIds(Set<String> messageIds, int maxResults) {
        Set<String> limited = new LinkedHashSet<>();
        for (String messageId : messageIds) {
            if (limited.size() >= maxResults) {
                break;
            }
            limited.add(messageId);
        }
        return limited;
    }

    @SuppressWarnings("unchecked")
    private void collectHistoryMessageIds(Map<?, ?> historyMap, String key, Set<String> messageIds) {
        Object entries = historyMap.get(key);
        if (!(entries instanceof List<?> list)) {
            return;
        }
        for (Object entry : list) {
            if (entry instanceof Map<?, ?> entryMap) {
                Object message = entryMap.get("message");
                if (message instanceof Map<?, ?> messageMap && messageMap.get("id") != null) {
                    messageIds.add(String.valueOf(messageMap.get("id")));
                } else if (entryMap.get("id") != null) {
                    messageIds.add(String.valueOf(entryMap.get("id")));
                }
            }
        }
    }

    private GmailSyncResult fetchMessages(MailAccountVO account, Set<String> messageIds, String latestHistoryId) {
        GmailSyncResult result = new GmailSyncResult();
        result.setLatestHistoryId(latestHistoryId);
        for (String messageId : messageIds) {
            Map<?, ?> response = fetchMessageMap(account, messageId);
            if (response == null) {
                continue;
            }
            GmailSyncedMessage message = toSyncedMessage(account, response);
            result.getMessages().add(message);
            if (message.getHistoryId() != null) {
                result.setLatestHistoryId(message.getHistoryId());
            }
        }
        return result;
    }

    private Map<?, ?> fetchMessageMap(MailAccountVO account, String messageId) {
        return webClient()
                .get()
                .uri(GMAIL_API + "/" + messageId + "?format=full")
                .headers(headers -> headers.setBearerAuth(accessToken(account)))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    private GmailSyncedMessage toSyncedMessage(MailAccountVO account, Map<?, ?> response) {
        GmailSyncedMessage message = new GmailSyncedMessage();
        message.setExternalMessageId(value(response.get("id")));
        message.setThreadId(value(response.get("threadId")));
        message.setHistoryId(value(response.get("historyId")));
        message.setSnippet(value(response.get("snippet")));
        message.setInternalDate(toLocalDateTime(value(response.get("internalDate"))));

        if (response.get("labelIds") instanceof List<?> labelIds) {
            for (Object labelId : labelIds) {
                if (labelId != null) {
                    message.getLabels().add(String.valueOf(labelId));
                }
            }
        }

        Object payload = response.get("payload");
        if (payload instanceof Map<?, ?> payloadMap) {
            message.setSubject(header(payloadMap, "Subject"));
            message.setMessageIdHeader(header(payloadMap, "Message-ID"));
            message.setFromEmail(header(payloadMap, "From"));
            message.setReplyToEmail(header(payloadMap, "Reply-To"));
            message.setTo(splitRecipients(header(payloadMap, "To")));
            message.setCc(splitRecipients(header(payloadMap, "Cc")));
            message.setBcc(splitRecipients(header(payloadMap, "Bcc")));
            message.setSentAt(parseDateHeader(header(payloadMap, "Date"), message.getInternalDate()));
            message.setContent(extractBody(response));
            message.setAttachments(extractAttachments(account, message.getExternalMessageId(), payloadMap));
        }
        return message;
    }

    private LocalDateTime toLocalDateTime(String epochMillis) {
        if (epochMillis == null || epochMillis.isBlank()) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(epochMillis)), ZoneId.systemDefault());
    }

    private LocalDateTime parseDateHeader(String value, LocalDateTime fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return java.time.ZonedDateTime.parse(value, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toLocalDateTime();
        } catch (Exception e) {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private String header(Map<?, ?> payloadMap, String headerName) {
        Object headers = payloadMap.get("headers");
        if (!(headers instanceof List<?> headerList)) {
            return null;
        }
        for (Object header : headerList) {
            if (header instanceof Map<?, ?> headerMap
                    && headerName.equalsIgnoreCase(value(headerMap.get("name")))) {
                return value(headerMap.get("value"));
            }
        }
        return null;
    }

    private List<String> splitRecipients(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> recipients = new ArrayList<>();
        for (String value : raw.split(",")) {
            if (!value.isBlank()) {
                recipients.add(value.trim());
            }
        }
        return recipients;
    }

    private List<GmailSyncedAttachment> extractAttachments(MailAccountVO account, String messageId, Map<?, ?> payloadMap) {
        List<Map<?, ?>> parts = new ArrayList<>();
        collectBodyCandidates(payloadMap, parts);
        List<GmailSyncedAttachment> attachments = new ArrayList<>();
        for (Map<?, ?> part : parts) {
            String filename = value(part.get("filename"));
            if (filename == null || filename.isBlank()) {
                continue;
            }
            String data = attachmentData(account, messageId, part);
            if (data == null || data.isBlank()) {
                continue;
            }
            attachments.add(new GmailSyncedAttachment(
                    filename,
                    value(part.get("mimeType")),
                    Base64.getUrlDecoder().decode(padBase64(data))));
        }
        return attachments;
    }

    private String attachmentData(MailAccountVO account, String messageId, Map<?, ?> part) {
        Object body = part.get("body");
        if (!(body instanceof Map<?, ?> bodyMap)) {
            return null;
        }
        String data = value(bodyMap.get("data"));
        if (data != null && !data.isBlank()) {
            return data;
        }
        String attachmentId = value(bodyMap.get("attachmentId"));
        if (attachmentId == null || attachmentId.isBlank()) {
            return null;
        }
        Map<?, ?> response = fetchAttachmentMap(account, messageId, attachmentId);
        return value(response == null ? null : response.get("data"));
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> fetchAttachmentMap(MailAccountVO account, String messageId, String attachmentId) {
        String responseBody = webClient()
                .get()
                .uri(GMAIL_API + "/" + messageId + "/attachments/" + attachmentId)
                .headers(headers -> headers.setBearerAuth(accessToken(account)))
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.createException().flatMap(Mono::error);
                    }
                    return DataBufferUtils.join(response.bodyToFlux(DataBuffer.class), GMAIL_MAX_IN_MEMORY_SIZE)
                            .map(this::toStringAndRelease);
                })
                .block();
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(responseBody, Map.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    private String toStringAndRelease(DataBuffer dataBuffer) {
        try {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private void modifyLabels(MailAccountVO account,
                              String externalMessageId,
                              List<String> addLabelIds,
                              List<String> removeLabelIds) {
        try {
            webClient()
                    .post()
                    .uri(GMAIL_API + "/" + externalMessageId + "/modify")
                    .headers(headers -> headers.setBearerAuth(accessToken(account)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "addLabelIds", addLabelIds,
                            "removeLabelIds", removeLabelIds))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (CustomException e) {
            throw e;
        } catch (WebClientResponseException e) {
            logGmailApiFailure("modify-labels", e);
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    private void postWithoutBody(MailAccountVO account, String uri) {
        try {
            webClient()
                    .post()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(accessToken(account)))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (CustomException e) {
            throw e;
        } catch (WebClientResponseException e) {
            logGmailApiFailure("post-without-body", e);
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    private String accessToken(MailAccountVO account) {
        if (account.getTokenExprDt() == null
                || account.getTokenExprDt().isAfter(LocalDateTime.now().plusSeconds(60))) {
            return account.getAccessToken();
        }
        if (account.getRefreshToken() == null || account.getRefreshToken().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        GoogleTokenResponse response = webClient()
                .post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("refresh_token", account.getRefreshToken())
                        .with("client_id", properties.getClientId())
                        .with("client_secret", properties.getClientSecret())
                        .with("grant_type", "refresh_token"))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        if (response == null || response.getAccessToken() == null) {
            mailMapper.updateMailAccountTokens(account.getEmpId(), account.getAccessToken(), account.getTokenExprDt(), "INVALID");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(
                response.getExpiresIn() == null ? 3600 : response.getExpiresIn());
        mailMapper.updateMailAccountTokens(account.getEmpId(), response.getAccessToken(), expiresAt, "ACTIVE");
        account.setAccessToken(response.getAccessToken());
        account.setTokenExprDt(expiresAt);
        return response.getAccessToken();
    }

    private String buildMimeMessage(GmailSendCommand command) throws Exception {
        if (command.getAttachments() == null || command.getAttachments().isEmpty()) {
            return buildSimpleMime(command);
        }
        return buildMultipartMime(command);
    }

    private String buildSimpleMime(GmailSendCommand command) {
        StringBuilder builder = baseHeaders(command);
        builder.append("Content-Type: text/html; charset=UTF-8\r\n");
        builder.append("Content-Transfer-Encoding: 8bit\r\n\r\n");
        builder.append(command.getContent());
        return builder.toString();
    }

    private String buildMultipartMime(GmailSendCommand command) throws Exception {
        String boundary = "mycrew-mail-" + UUID.randomUUID();
        StringBuilder builder = baseHeaders(command);
        builder.append("MIME-Version: 1.0\r\n");
        builder.append("Content-Type: multipart/mixed; boundary=\"").append(boundary).append("\"\r\n\r\n");
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Type: text/html; charset=UTF-8\r\n");
        builder.append("Content-Transfer-Encoding: 8bit\r\n\r\n");
        builder.append(command.getContent()).append("\r\n");

        for (MultipartFile file : command.getAttachments()) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String contentType = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
            String encoded = Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.UTF_8))
                    .encodeToString(file.getBytes());
            builder.append("--").append(boundary).append("\r\n");
            builder.append("Content-Type: ").append(contentType)
                    .append("; name=\"").append(escapeHeader(file.getOriginalFilename())).append("\"\r\n");
            builder.append("Content-Disposition: attachment; filename=\"")
                    .append(escapeHeader(file.getOriginalFilename())).append("\"\r\n");
            builder.append("Content-Transfer-Encoding: base64\r\n\r\n");
            builder.append(encoded).append("\r\n");
        }
        builder.append("--").append(boundary).append("--");
        return builder.toString();
    }

    private StringBuilder baseHeaders(GmailSendCommand command) {
        StringBuilder builder = new StringBuilder();
        appendHeader(builder, "From", command.getFromEmail());
        appendHeader(builder, "To", String.join(", ", command.getTo()));
        if (command.getCc() != null && !command.getCc().isEmpty()) {
            appendHeader(builder, "Cc", String.join(", ", command.getCc()));
        }
        if (command.getBcc() != null && !command.getBcc().isEmpty()) {
            appendHeader(builder, "Bcc", String.join(", ", command.getBcc()));
        }
        appendHeader(builder, "Subject", command.getSubject());
        return builder;
    }

    private void appendHeader(StringBuilder builder, String name, String value) {
        builder.append(name).append(": ").append(value == null ? "" : value).append("\r\n");
    }

    private String escapeHeader(String value) {
        if (value == null || value.isBlank()) {
            return "attachment";
        }
        return value.replace("\"", "");
    }

    @SuppressWarnings("unchecked")
    private String extractBody(Map<?, ?> response) {
        Object payload = response.get("payload");
        if (!(payload instanceof Map<?, ?> payloadMap)) {
            return null;
        }
        List<Map<?, ?>> candidates = new ArrayList<>();
        collectBodyCandidates(payloadMap, candidates);
        for (Map<?, ?> candidate : candidates) {
            if ("text/html".equals(value(candidate.get("mimeType")))) {
                return decodeBody(candidate);
            }
        }
        for (Map<?, ?> candidate : candidates) {
            if ("text/plain".equals(value(candidate.get("mimeType")))) {
                return decodeBody(candidate);
            }
        }
        return decodeBody(payloadMap);
    }

    @SuppressWarnings("unchecked")
    private void collectBodyCandidates(Map<?, ?> part, List<Map<?, ?>> candidates) {
        candidates.add(part);
        Object parts = part.get("parts");
        if (parts instanceof List<?> partList) {
            for (Object child : partList) {
                if (child instanceof Map<?, ?> childMap) {
                    collectBodyCandidates(childMap, candidates);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String decodeBody(Map<?, ?> part) {
        Object body = part.get("body");
        if (!(body instanceof Map<?, ?> bodyMap)) {
            return null;
        }
        String data = value(bodyMap.get("data"));
        if (data == null || data.isBlank()) {
            return null;
        }
        byte[] decoded = Base64.getUrlDecoder().decode(padBase64(data));
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String padBase64(String value) {
        int mod = value.length() % 4;
        if (mod == 0) {
            return value;
        }
        return value + "=".repeat(4 - mod);
    }

    private String value(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
