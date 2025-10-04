package com.nakqeeb.amancare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * إعداد جوانب AOP لتفعيل Activity Logging
 * Aspect Configuration to enable Activity Logging
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectConfiguration {
    // AspectJ auto-proxy is now enabled for activity logging
}