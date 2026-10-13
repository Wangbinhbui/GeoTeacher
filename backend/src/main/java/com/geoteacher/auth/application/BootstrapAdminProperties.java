package com.geoteacher.auth.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geoteacher.bootstrap-admin")
public record BootstrapAdminProperties(
        String username,
        String password,
        String displayName
) {
}
