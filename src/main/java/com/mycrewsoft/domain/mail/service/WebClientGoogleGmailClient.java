package com.mycrewsoft.domain.mail.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.mail.config.GoogleOAuthProperties;
import com.mycrewsoft.domain.mail.dto.response.GoogleTokenResponse;
import com.mycrewsoft.domain.mail.gmail.GmailMessageContent;
import com.mycrewsoft.domain.mail.gmail.GmailSendCommand;
import com.mycrewsoft.domain.mail.gmail.GmailSendResult;
import com.mycrewsoft.domain.mail.mapper.MailMapper;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebClientGoogleGmailClient implements GoogleGmailClient {

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String GMAIL_API = "https://gmail.googleapis.com/gmail/v1/users/me/messages";

    private final WebClient.Builder webClientBuilder;
    private final GoogleOAuthProperties properties;
    private final MailMapper mailMapper;

    @Override
    public GmailSendResult sendMessage(MailAccountVO account, GmailSendCommand command) {
        try {
            String raw = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(buildMimeMessage(command).getBytes(StandardCharsets.UTF_8));

            Map<?, ?> response = webClientBuilder.build()
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
            Map<?, ?> response = webClientBuilder.build()
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
            webClientBuilder.build()
                    .delete()
                    .uri(GMAIL_API + "/" + externalMessageId)
                    .headers(headers -> headers.setBearerAuth(accessToken(account)))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    private void modifyLabels(MailAccountVO account,
                              String externalMessageId,
                              List<String> addLabelIds,
                              List<String> removeLabelIds) {
        try {
            webClientBuilder.build()
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
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MAIL_SYNC_FAILED);
        }
    }

    private void postWithoutBody(MailAccountVO account, String uri) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(accessToken(account)))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (CustomException e) {
            throw e;
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

        GoogleTokenResponse response = webClientBuilder.build()
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
