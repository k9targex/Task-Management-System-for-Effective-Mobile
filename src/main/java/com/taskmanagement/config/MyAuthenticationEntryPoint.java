package com.taskmanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanagement.exception.ControllerExceptionHandler;
import com.taskmanagement.model.dto.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ControllerExceptionHandler controllerExceptionHandler;

  public MyAuthenticationEntryPoint(ControllerExceptionHandler controllerExceptionHandler) {
    this.controllerExceptionHandler = controllerExceptionHandler;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    WebRequest webRequest = new ServletWebRequest(request);
    ResponseError error =
        controllerExceptionHandler.handleInsufflicientException(authException, webRequest);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    response.setContentType("application/json");

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.writeValue(response.getWriter(), error);
  }
}
