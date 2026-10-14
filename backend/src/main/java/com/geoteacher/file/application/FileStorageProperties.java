package com.geoteacher.file.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "geoteacher.file-storage")
public record FileStorageProperties(Path root) {
}
