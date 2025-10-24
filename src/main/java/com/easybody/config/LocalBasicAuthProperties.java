package com.easybody.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth.basic")
public class LocalBasicAuthProperties {

    /**
     * Enables HTTP Basic authentication for local development.
     */
    private boolean enabled = false;

    /**
     * Username used for local HTTP Basic authentication.
     */
    private String username = "local-admin";

    /**
     * Password used for local HTTP Basic authentication.
     */
    private String password = "local-password";

    /**
     * Authority granted to the local HTTP Basic user.
     */
    private String role = "ADMIN";
}
