package com.mycrewsoft.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 모든 요청의 메서드, URL, IP, 처리 시간, 응답 상태코드를 자동으로 로그에 출력한다.
 * 출력 예시:
 * 요청: [GET] /api/v1/users/1 - IP: 127.0.0.1
 * 응답: [GET] /api/v1/users/1 - Status: 200 | 23ms
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    /**
     * Controller 진입 직전에 실행된다.
     * 요청 시작 시각을 request attribute 에 저장하고, 요청 정보를 로그에 출력한다.
     * false 를 반환하면 Controller 에 진입하지 않는다. 여기서는 항상 true 를 반환한다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @param handler  실행될 핸들러 (Controller 메서드)
     * @return true (항상 Controller 진입 허용)
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        log.info("요청(Request): [{}] {} - IP: {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

        return true;
    }

    /**
     * View 렌더링까지 완료된 후 실행된다.
     * 요청 처리 시간과 응답 상태코드를 로그에 출력한다.
     * 예외가 발생한 경우 예외 메시지도 함께 출력한다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @param handler  실행된 핸들러
     * @param ex       처리 중 발생한 예외 (없으면 null)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - (Long) request.getAttribute(START_TIME);
        log.info("응답(Response): [{}] {} - Status:{} | {}ms",
                request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        if (ex != null) log.error("예외 발생: {}", ex.getMessage());
    }
}