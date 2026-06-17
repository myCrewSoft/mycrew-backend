package com.mycrewsoft.domain.mail.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mycrewsoft.domain.mail.dto.response.MailAttachmentResponse;
import com.mycrewsoft.domain.mail.dto.response.MailDetailResponse;
import com.mycrewsoft.domain.mail.dto.response.MailLabelResponse;
import com.mycrewsoft.domain.mail.dto.response.MailParticipantResponse;
import com.mycrewsoft.domain.mail.dto.response.MailSummaryResponse;
import com.mycrewsoft.domain.mail.vo.MailAccountVO;
import com.mycrewsoft.domain.mail.vo.MailMessageRow;
import com.mycrewsoft.domain.mail.vo.MailParticipantRow;

@Mapper
public interface MailMapper {

    MailAccountVO selectActiveMailAccount(@Param("empId") Long empId);

    java.util.List<MailAccountVO> selectActiveMailAccounts();

    Long selectNextMailMessageId();

    Long selectNextMailLabelId();

    Long selectNextMailLabelMapId();

    Long selectNextMailParticipantId();

    void insertMailMessage(MailMessageRow row);

    void updateMailBody(@Param("empId") Long empId,
                        @Param("mailId") Long mailId,
                        @Param("content") String content,
                        @Param("snippet") String snippet);

    void updateMailMessage(MailMessageRow row);

    MailMessageRow selectMailRow(@Param("empId") Long empId, @Param("mailId") Long mailId);

    Long selectMailIdByExternalMessageId(@Param("empId") Long empId,
                                         @Param("externalMessageId") String externalMessageId);

    MailDetailResponse selectMailDetail(@Param("empId") Long empId, @Param("mailId") Long mailId);

    List<MailSummaryResponse> selectMails(@Param("empId") Long empId,
                                          @Param("type") String type,
                                          @Param("keyword") String keyword,
                                          @Param("emailAddr") String emailAddr,
                                          @Param("offset") long offset,
                                          @Param("size") int size);

    long countUnreadInbox(@Param("empId") Long empId);

    long countMails(@Param("empId") Long empId,
                    @Param("type") String type,
                    @Param("keyword") String keyword,
                    @Param("emailAddr") String emailAddr);

    List<MailSummaryResponse> selectTrash(@Param("empId") Long empId,
                                          @Param("offset") long offset,
                                          @Param("size") int size);

    long countTrash(@Param("empId") Long empId);

    List<MailParticipantResponse> selectParticipants(@Param("empId") Long empId, @Param("mailId") Long mailId);

    List<MailAttachmentResponse> selectAttachments(@Param("empId") Long empId, @Param("mailId") Long mailId);

    MailAttachmentResponse selectAttachmentMeta(@Param("empId") Long empId, @Param("mailId") Long mailId, @Param("attachmentId") Long attachmentId);

    List<String> selectLabelTypes(@Param("empId") Long empId, @Param("mailId") Long mailId);

    List<MailMessageRow> selectTrashRows(@Param("empId") Long empId);

    void mergeSystemLabel(@Param("labelId") Long labelId,
                          @Param("empId") Long empId,
                          @Param("externalLabelId") String externalLabelId,
                          @Param("labelName") String labelName,
                          @Param("labelTypeCd") String labelTypeCd);

    Long selectLabelIdByType(@Param("empId") Long empId, @Param("labelTypeCd") String labelTypeCd);

    int existsLabelMap(@Param("empId") Long empId,
                       @Param("mailId") Long mailId,
                       @Param("labelId") Long labelId);

    void insertLabelMap(@Param("labelMapId") Long labelMapId,
                        @Param("empId") Long empId,
                        @Param("mailId") Long mailId,
                        @Param("labelId") Long labelId);

    void deleteLabelMapByType(@Param("empId") Long empId,
                              @Param("mailId") Long mailId,
                              @Param("labelTypeCd") String labelTypeCd);

    void deleteLabelMapsByMail(@Param("empId") Long empId, @Param("mailId") Long mailId);

    java.util.List<MailLabelResponse> selectUserLabels(@Param("empId") Long empId);

    MailLabelResponse selectUserLabel(@Param("empId") Long empId, @Param("labelId") Long labelId);

    int countUserLabelName(@Param("empId") Long empId, @Param("name") String name);

    void insertUserLabel(@Param("labelId") Long labelId, @Param("empId") Long empId, @Param("name") String name);

    int updateUserLabelName(@Param("empId") Long empId, @Param("labelId") Long labelId, @Param("name") String name);

    int softDeleteUserLabel(@Param("empId") Long empId, @Param("labelId") Long labelId);

    void deleteLabelMapsByLabel(@Param("empId") Long empId, @Param("labelId") Long labelId);

    void deleteLabelMapByLabelId(@Param("empId") Long empId, @Param("mailId") Long mailId, @Param("labelId") Long labelId);

    long countMailsByLabel(@Param("empId") Long empId, @Param("labelId") Long labelId);

    java.util.List<MailSummaryResponse> selectMailsByLabel(@Param("empId") Long empId, @Param("labelId") Long labelId, @Param("offset") long offset, @Param("size") int size);

    java.util.List<MailLabelResponse> selectUserLabelsForMail(@Param("empId") Long empId, @Param("mailId") Long mailId);

    void markMessagesDeleted(@Param("empId") Long empId, @Param("mailIds") List<Long> mailIds);

    void insertParticipant(MailParticipantRow row);

    void deleteParticipantsByMail(@Param("empId") Long empId, @Param("mailId") Long mailId);

    void insertAttachment(@Param("attachmentId") Long attachmentId,
                          @Param("empId") Long empId,
                          @Param("mailId") Long mailId);

    void updateMailAccountTokens(@Param("empId") Long empId,
                                 @Param("accessToken") String accessToken,
                                 @Param("tokenExprDt") LocalDateTime tokenExprDt,
                                 @Param("tokenStatusCd") String tokenStatusCd);

    void updateMailAccountSyncState(@Param("empId") Long empId,
                                    @Param("googleHistoryId") String googleHistoryId);
}
