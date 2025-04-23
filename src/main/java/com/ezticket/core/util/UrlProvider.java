package com.ezticket.core.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class UrlProvider {

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${ngrok.url}")
    private String ngrokUrl;

    @Value("${server.port}")
    private String serverPort;

    @Getter
    private String returnURL;
    @Getter
    private String localURL;

    @PostConstruct
    public void init() {
        this.returnURL = getBaseUrl();
        this.localURL = getLocalHostUrl();
    }

    private String getBaseUrl() {
        if ("dev".equalsIgnoreCase(active)) {
            // 本機測試環境，使用 Ngrok URL
            return ngrokUrl;
        } else {
            // 正式環境，自動抓取部屬的主機名稱
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                String ipAddress = localHost.getHostAddress();
                return "https://" + ipAddress + ":" + serverPort;
            } catch (UnknownHostException e) {
                throw new RuntimeException("無法取得主機名稱", e);
            }
        }
    }

    private String getLocalHostUrl() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            return "http://" + ipAddress + ":" + serverPort;
        } catch (UnknownHostException e) {
            throw new RuntimeException("無法取得主機名稱", e);
        }
    }
}