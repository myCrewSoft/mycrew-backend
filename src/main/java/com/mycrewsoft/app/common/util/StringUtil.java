package com.mycrewsoft.app.common.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * 문자열 관련 유틸리티 클래스.
 * 범용 처리는 Commons Lang3 StringUtils 에 위임하고,
 * 프로젝트 특화 기능(마스킹, 검증, XSS 방지)만 직접 구현한다.
 * 모든 메서드는 static 이며, 인스턴스 생성을 금지한다.
 */
public final class StringUtil {

    private StringUtil() {}

    /**
     * 이메일 형식이 유효한지 검증한다.
     * blank 이면 false 를 반환한다.
     * 영문자, 숫자, +_.- 를 허용하며 @ 뒤 도메인과 TLD 가 존재해야 한다.
     *
     * @param email 검증할 이메일 문자열
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) return false;

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * 이메일 주소를 개인정보 보호 목적으로 마스킹 처리한다.
     * @ 앞 로컬 파트의 일부를 * 로 대체한다.
     * 예) hong@gmail.com → hon***@gmail.com
     *
     * @param email 마스킹할 이메일 문자열
     * @return 마스킹된 이메일. blank 이거나 @ 가 없으면 원본 반환
     */
    public static String maskEmail(String email) {
        if (StringUtils.isBlank(email) || !email.contains("@")) return email;
        String[] parts = email.split("@", 2);
        String local = parts[0];
        int showLen = local.length() <= 3 ? 1 : 3;

        return local.substring(0, showLen)
                + StringUtils.repeat("*", local.length() - showLen)
                + "@" + parts[1];
    }

    /**
     * 이름을 개인정보 보호 목적으로 마스킹 처리한다.
     * 첫 글자와 마지막 글자만 보이고 중간을 * 로 대체한다.
     * 예) 홍길동 → 홍*동
     * 예) 김철 → 김*
     * 예) 김 → 김 (1글자는 그대로 반환)
     *
     * @param name 마스킹할 이름 문자열
     * @return 마스킹된 이름. blank 면 원본 반환
     */
    public static String maskName(String name) {
        if (StringUtils.isBlank(name)) return name;
        if (name.length() == 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";

        return name.charAt(0)
                + StringUtils.repeat("*", name.length() - 2)
                + name.charAt(name.length() - 1);
    }

    /**
     * 전화번호에서 숫자만 추출해 정규화된 형태로 반환한다.
     * 하이픈, 괄호, 공백 등 숫자가 아닌 문자를 모두 제거한다.
     * 예) "010-1234-5678" → "01012345678"
     * 예) "(02) 1234-5678" → "0212345678"
     *
     * @param phone 정규화할 전화번호 문자열
     * @return 숫자만 남은 전화번호. blank 면 빈 문자열 반환
     */
    public static String normalizePhone(String phone) {
        if (StringUtils.isBlank(phone)) return StringUtils.EMPTY;

        return phone.replaceAll("[^0-9]", "");
    }

    /**
     * 휴대폰 번호 형식이 유효한지 검증한다.
     * 010, 011, 016, 017, 018, 019 로 시작하는 10~11 자리 번호를 허용한다.
     * 먼저 normalizePhone() 으로 정규화한 후 검증한다.
     *
     * @param phone 검증할 전화번호 문자열
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValidPhone(String phone) {
        return normalizePhone(phone).matches("^01[016789][0-9]{7,8}$");
    }

    /**
     * 전화번호를 개인정보 보호 목적으로 마스킹 처리한다.
     * 정규화 후 가운데 4자리를 * 로 대체한다.
     * 예) "010-1234-5678" → "010-****-5678"
     * 정규화 후 11자리가 아니면 원본을 반환한다.
     *
     * @param phone 마스킹할 전화번호 문자열
     * @return 마스킹된 전화번호
     */
    public static String maskPhone(String phone) {
        String normalized = normalizePhone(phone);
        if (normalized.length() != 11) return phone;

        return normalized.substring(0, 3) + "-****-" + normalized.substring(7);
    }

    /**
     * HTML 특수문자를 이스케이프하여 XSS 공격을 방지한다.
     * Spring 내장 HtmlUtils 를 사용.
     * 예) <script>alert('xss')</script>
     * → &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;
     *
     * @param input 이스케이프할 문자열
     * @return 이스케이프된 문자열. blank 면 원본 반환
     */
    public static String escapeHtml(String input) {
        if (StringUtils.isBlank(input)) return input;

        return HtmlUtils.htmlEscape(input);
    }

    /**
     * 이스케이프된 HTML 문자열을 원래 형태로 복원한다.
     * DB 에 이스케이프된 값이 저장된 경우 원본 값으로 복원할 때 사용한다.
     *
     * @param input 복원할 문자열
     * @return 복원된 문자열. blank 면 원본 반환
     */
    public static String unescapeHtml(String input) {
        if (StringUtils.isBlank(input)) return input;
        
        return HtmlUtils.htmlUnescape(input);
    }
}