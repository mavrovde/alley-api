package de.alley.maverick.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
@Service
public class StarterSynchronizer {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port}")
    private String elasticsearchPort;

    @Value("${elasticsearch.timeout}")
    private String elasticsearchTimeout;

    private final FilesSynchronizer fileSync;

    @Autowired
    public StarterSynchronizer(FilesSynchronizer fileSync) {
        this.fileSync = fileSync;
    }

    @Scheduled(fixedDelay = 600000, initialDelay = 600000)
    public void syncFiles() {
        log.info("Synchronization is started...");
        fileSync.asyncFiles();
    }

    public boolean checkElasticsearch() {
        log.info("Checking elasticsearch for init synchronization");
        try {
            try (Socket elSocket = new Socket()) {
                // Connects this socket to the server with a specified timeout value.
                elSocket.connect(new InetSocketAddress(elasticsearchHost, Integer.parseInt(elasticsearchPort)),
                        Integer.parseInt(elasticsearchTimeout));
            }
            // Return true if connection successful
            return true;
        } catch (IOException exception) {
            log.warn("Elasticsearch is unreachable for init synchronization");
            // Return false if connection fails
            return false;
        }
    }
}
