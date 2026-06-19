package com.mycrewsoft.domain.mail.gmail;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GmailLabelChange {

    private String externalMessageId;
    private List<String> addedLabels = new ArrayList<>();
    private List<String> removedLabels = new ArrayList<>();

    public GmailLabelChange() {
    }

    public GmailLabelChange(String externalMessageId, List<String> addedLabels, List<String> removedLabels) {
        this.externalMessageId = externalMessageId;
        this.addedLabels = addedLabels == null ? new ArrayList<>() : new ArrayList<>(addedLabels);
        this.removedLabels = removedLabels == null ? new ArrayList<>() : new ArrayList<>(removedLabels);
    }
}
