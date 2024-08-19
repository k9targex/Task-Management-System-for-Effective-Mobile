package com.taskmanagement.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PointcutDefinitions {

  // Точка среза для регистрации
  @Pointcut("execution(* com.taskmanagement.controller.SecurityController.signup(..))")
  public void signupPointcut() {}

  // Точка среза для входа
  @Pointcut("execution(* com.taskmanagement.controller.SecurityController.signin(..))")
  public void signinPointcut() {}

  @Pointcut("execution(* com.taskmanagement.controller.*.*(..))")
  public void getPointcut() {}
}
