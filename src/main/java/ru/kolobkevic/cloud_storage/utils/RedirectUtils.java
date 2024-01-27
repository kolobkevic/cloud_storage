package ru.kolobkevic.cloud_storage.utils;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class RedirectUtils {
    public String getRedirectPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8).replace('+', ' ');
    }
}
