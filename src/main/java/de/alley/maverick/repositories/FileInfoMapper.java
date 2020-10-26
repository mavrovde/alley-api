package de.alley.maverick.repositories;

import com.dropbox.core.v2.files.FileMetadata;
import de.alley.maverick.protocol.FileInfo;

public final class FileInfoMapper {

    private FileInfoMapper() {
    }

    public static FileInfo map(FileMetadata meta) {
        return FileInfo.builder()
                .id(meta.getId())
                .name(meta.getName())
                .path(meta.getPathLower())
                .size(meta.getSize())
                .build();
    }

}
