package de.alley.maverick.services;

import de.alley.maverick.protocol.FileInfo;
import de.alley.maverick.repositories.DropboxRepo;
import de.alley.maverick.repositories.ElasticsearchRepo;
import de.alley.maverick.repositories.Mode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FileService {

    @Value("dropbox.direct")
    private String dropboxDirect;

    @Autowired
    private Environment env;

    private final ElasticsearchRepo esRepo;
    private final DropboxRepo dbRepo;
    private final FilesSynchronizer sync;

    @Autowired
    public FileService(ElasticsearchRepo esRepo, DropboxRepo dbRepo, FilesSynchronizer sync) {
        this.esRepo = esRepo;
        this.dbRepo = dbRepo;
        this.sync = sync;
    }

    public Optional<FileInfo> findById(String fileId) {
        Optional<FileInfo> result = esRepo.findElasticsearchRecordById(fileId);
        if (result.isEmpty()) {
            Optional<FileInfo> dbFile = dbRepo.findDropboxFileById(fileId);
            log.debug(String.format("The file %s has been found in dropbox", fileId));
            if (dbFile.isPresent()) {
                esRepo.createElasticsearchRecordIfNotExists(dbFile.get());
                result = esRepo.findElasticsearchRecordById(fileId);
            }
        }
        return result;
    }

    public Optional<FileInfo> resetTags(String fileId, List<String> tags) {
        Optional<FileInfo> file = findById(fileId);
        return file.flatMap(f -> esRepo.updateElasticsearchRecord(f, tags, Mode.RESET));
    }

    public Optional<FileInfo> mergeTags(String fileId, List<String> tags) {
        Optional<FileInfo> file = findById(fileId);
        return file.flatMap(f -> esRepo.updateElasticsearchRecord(f, tags, Mode.MERGE));
    }

    public Optional<FileInfo> deleteTags(String fileId, List<String> tags) {
        Optional<FileInfo> file = findById(fileId);
        return file.flatMap(f -> esRepo.updateElasticsearchRecord(f, tags, Mode.DELETE));
    }

    public List<FileInfo> search(String fileName) {
        if (Boolean.parseBoolean(env.getProperty("DROPBOX_DIRECT", dropboxDirect))) {
            List<FileInfo> dbRes = dbRepo.findDropboxFilesByName(fileName);
            esRepo.createElasticSearchRecordsIfNotExists(dbRes);
        }
        return esRepo.findByPartName(fileName);
    }
}
