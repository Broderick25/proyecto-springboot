package com.SpringBoot.application.port.outbound;

import java.net.URL;

public interface StoragePort {

    void upload(String key, byte[] content, String contentType);

    URL generatePresignedGetUrl(String key);
}
