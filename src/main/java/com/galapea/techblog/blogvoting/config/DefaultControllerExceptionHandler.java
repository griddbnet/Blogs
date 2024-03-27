package com.galapea.techblog.blogvoting.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class DefaultControllerExceptionHandler {
    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> defaultExceptionHandler(final Exception e, final HttpServletRequest req) {
        final String message = e.getMessage() != null ? e.getMessage() : "";
        log.error(message, e);
        final Map<String, Object> result = new HashMap<>();
        result.put("errorType", e.getClass().getSimpleName());
        return result;
    }
}
