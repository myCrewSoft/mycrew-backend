package com.mycrewsoft.domain.search.factory;

import com.mycrewsoft.domain.search.dto.response.MailSearchDetails;
import com.mycrewsoft.domain.search.dto.response.SearchResultDetails;
import com.mycrewsoft.domain.search.enums.SearchType;
import com.mycrewsoft.domain.search.vo.SearchVO;
import org.springframework.stereotype.Component;

@Component
public class MailSearchResultFactory extends SearchResultFactory {
    @Override
    public SearchType getSupportedType() {
        return SearchType.MAIL;
    }

    @Override
    protected String createUrl(SearchVO vo) {
        return "/mail/" + vo.getId();
    }

    @Override
    protected SearchResultDetails createDetails(SearchVO vo) {
        return MailSearchDetails.builder()
                .senderName(vo.getSenderName())
                .senderAddress(vo.getSenderAddress())
                .receivedAt(vo.getReceivedAt())
                .read(vo.getMailRead())
                .hasAttachment(vo.getHasAttachment())
                .bodyPreview(vo.getBodyPreview())
                .build();
    }
}
