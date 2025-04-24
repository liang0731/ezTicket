package com.ezticket.core.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class UrlProvider {

    @Value("${ecpay.return.url}")
    @Getter
    private String returnURL;

    @Value("${server.port}")
    private String serverPort;

    @Getter
    private String localURL;

    @PostConstruct
    public void init() {
        this.localURL = buildLocalHostUrl();
    }

    private String buildLocalHostUrl() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return "http://" + localHost.getHostAddress() + ":" + serverPort;
        } catch (Exception e) {
            throw new RuntimeException("無法取得本機 IP", e);
        }
    }
}