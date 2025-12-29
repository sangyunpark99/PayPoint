package org.sangyunpark.pointpay.common.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyRecord;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyService;
import org.sangyunpark.pointpay.domain.idempotency.IdempotencyStatus;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final String IDEMPOTENCY_KEY_ATTR = "IDEMPOTENCY_KEY";
    private static final String REQUEST_HEADER = "X-Idempotency-Key";

    private final IdempotencyService idempotencyService;

    public IdempotencyInterceptor(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);
        if(idempotent == null) {
            return true;
        }

        String key = request.getHeader(REQUEST_HEADER);
        if(key == null || key.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("Missing X-Idempotency-key header");
            return false;
        }

        Optional<IdempotencyRecord> cached = idempotencyService.find(key);
        if(cached.isPresent() && cached.get().getStatus().equals(IdempotencyStatus.SUCCESS)) {
            writeResponse(response, cached.get());
            return false;
        }

        boolean acquired = idempotencyService.progress(key);
        if(!acquired) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("해당 요청은 이미 처리중입니다.");
            return false;
        }

        request.setAttribute(IDEMPOTENCY_KEY_ATTR, key);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object keyAttr = request.getAttribute(IDEMPOTENCY_KEY_ATTR);
        if (keyAttr == null) {
            return;
        }
        String key = keyAttr.toString();

        if (!(response instanceof ContentCachingResponseWrapper wrapper)) {
            return;
        }

        byte[] bodyBytes = wrapper.getContentAsByteArray();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        IdempotencyRecord record = IdempotencyRecord.completed(response.getStatus(), body);
        idempotencyService.complete(key, record);
    }

    private void writeResponse(HttpServletResponse response, IdempotencyRecord record) throws IOException {
        response.setStatus(record.getStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(record.getResponseBody());
    }
}