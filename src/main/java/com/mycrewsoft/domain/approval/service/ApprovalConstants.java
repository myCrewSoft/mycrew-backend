package com.mycrewsoft.domain.approval.service;

public final class ApprovalConstants {

    private ApprovalConstants() {
    }

    public static final String DOC_STATUS_TEMPORARY = "00";
    public static final String DOC_STATUS_IN_PROGRESS = "01";
    public static final String DOC_STATUS_COMPLETED = "02";
    public static final String DOC_STATUS_REJECTED = "03";
    public static final String DOC_STATUS_WITHDRAWN = "04";

    public static final String STEP_STATUS_WAITING = "01";
    public static final String STEP_STATUS_IN_PROGRESS = "02";
    public static final String STEP_STATUS_COMPLETED = "03";
    public static final String STEP_STATUS_REJECTED = "04";
    public static final String STEP_STATUS_SKIPPED = "05";

    public static final String LINE_STATUS_WAITING = "01";
    public static final String LINE_STATUS_APPROVED = "02";
    public static final String LINE_STATUS_REJECTED = "03";
    public static final String LINE_STATUS_SKIPPED = "04";
    public static final String LINE_STATUS_WITHDRAW_CANCELLED = "05";
}
