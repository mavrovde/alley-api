package de.alley.maverick.services;

import de.alley.maverick.protocol.FileInfo;
import de.alley.maverick.repositories.DropboxRepo;
import de.alley.maverick.repositories.ElasticsearchRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@EnableAsync
@Service
public class FilesSynchronizer {

    private final ElasticsearchRepo esRepo;
    private final DropboxRepo dbRepo;

    @Autowired
    public FilesSynchronizer(ElasticsearchRepo esRepo, DropboxRepo dbRepo) {
        this.esRepo = esRepo;
        this.dbRepo = dbRepo;
    }

    @Async("threadPoolTaskExecutor")
    public void asyncFiles() {
        List<FileInfo> dbFiles = dbRepo.findAllDropboxFiles();
        esRepo.createElasticSearchRecordsIfNotExists(dbFiles);
        log.info("Synchronization is finished...");
    }

}

