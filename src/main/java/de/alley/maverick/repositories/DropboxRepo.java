package de.alley.maverick.repositories;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchMatchV2;
import com.dropbox.core.v2.files.SearchV2Result;
import de.alley.maverick.protocol.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class DropboxRepo {

    private final DbxClientV2 dbClient;

    @Autowired
    public DropboxRepo(DbxClientV2 dbClient) {
        this.dbClient = dbClient;
    }

    public Optional<FileInfo> findDropboxFileById(String fileId) {
        try {
            FileMetadata actual = (FileMetadata) dbClient.files().getMetadata(fileId);
            return Optional.ofNullable(FileInfoMapper.map(actual));
        } catch (GetMetadataErrorException e) {
            log.warn(String.format("Cannot find the file %s from dropbox", fileId));
            return Optional.empty();
        } catch (DbxException e) {
            log.warn(String.format("Cannot get the file %s from dropbox", fileId));
            throw new IllegalStateException("Critical error in dropbox", e);
        }
    }

    public List<FileInfo> findDropboxFilesByName(String fileName) {
        try {
            SearchV2Result result = dbClient.files().searchV2(fileName);
            List<SearchMatchV2> matches = result.getMatches();
            List<FileInfo> founded = new ArrayList<>();
            for (SearchMatchV2 match : matches) {
                Metadata metadata = match.getMetadata().getMetadataValue();
                if (metadata instanceof FileMetadata) {
                    founded.add(FileInfoMapper.map((FileMetadata) metadata));
                }
            }
            return founded;
        } catch (GetMetadataErrorException e) {
            log.warn(String.format("Cannot find the file %s from dropbox", fileName));
            return new ArrayList<>();
        } catch (DbxException e) {
            log.warn(String.format("Cannot get the file %s from dropbox", fileName));
            throw new IllegalStateException("Critical error in dropbox", e);
        }
    }

    public List<FileInfo> findAllDropboxFiles() {
        try {
            List<FileInfo> result = new ArrayList<>();
            ListFolderBuilder listFolderBuilder = dbClient.files().listFolderBuilder("");
            ListFolderResult cursor = listFolderBuilder.withRecursive(true).start();
            while (true) {
                if (cursor != null) {
                    for (Metadata entry : cursor.getEntries()) {
                        if (entry instanceof FileMetadata) {
                            FileMetadata meta = (FileMetadata) entry;
                            log.debug(String.format("Found in dropbox file: %s:%s", meta.getId(), meta.getPathLower()));
                            result.add(FileInfoMapper.map(meta));
                        }
                    }
                    if (!cursor.getHasMore()) {
                        return result;
                    }
                    try {
                        cursor = dbClient.files().listFolderContinue(cursor.getCursor());
                    } catch (DbxException e) {
                        log.info("Couldn't get listFolderContinue");
                    }
                }
            }
        } catch (DbxException e) {
            log.warn("Cannot get all files from dropbox");
            throw new IllegalStateException("Critical error in dropbox", e);
        }
    }

}
