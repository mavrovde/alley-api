package de.alley.maverick.web;

import de.alley.maverick.protocol.FileInfo;
import de.alley.maverick.protocol.FileInfoModelAssembler;
import de.alley.maverick.protocol.TagForm;
import de.alley.maverick.services.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
public class FileRestController {

    private final FileService fileService;
    private final FileInfoModelAssembler fileAssembler;

    @Autowired
    public FileRestController(FileService fileService, FileInfoModelAssembler fileAssembler) {
        this.fileService = fileService;
        this.fileAssembler = fileAssembler;
    }

    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntityModel<FileInfo>>> search(@RequestParam(value = "fileName",
            required = false) final String fileName){
        return ResponseEntity.ok(fileService.search(fileName)
                .stream().map(fileAssembler::toModel).collect(Collectors.toList()));
    }

    @GetMapping(path = "/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<FileInfo>> getFileById(@Valid @PathVariable(name = "fileId") final String fileId){
        if (isNotValidFileId(fileId)) {
            return ResponseEntity.badRequest().build();
        }
        return fileService.findById(fileId)
                .map(fileAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/{fileId}/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<FileInfo>> resetTags(@Valid @PathVariable(name = "fileId") final String fileId, @RequestBody TagForm tagForm) {
        if (isNotValidFileId(fileId) || isNotValidTags(tagForm)) {
            return ResponseEntity.badRequest().build();
        }
        return fileService.resetTags(fileId, tagForm.getTags())
                .map(fileAssembler::toModel)
                .map(f -> createResponseCreated(f, linkTo(methodOn(this.getClass())
                        .getFileById(Objects.requireNonNull(f.getContent()).getId())).toString()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{fileId}/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> mergeTags(@Valid @PathVariable(name = "fileId") final String fileId, @RequestBody TagForm tagForm) {
        if (isNotValidFileId(fileId) || isNotValidTags(tagForm)) {
            return ResponseEntity.badRequest().build();
        }
        return fileService.mergeTags(fileId, tagForm.getTags())
                .map(f -> ResponseEntity.noContent().build())
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/{fileId}/tags", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteTags(@Valid @PathVariable(name = "fileId") final String fileId, @RequestBody TagForm tagForm) {
        if (isNotValidFileId(fileId) || isNotValidTags(tagForm)) {
            return ResponseEntity.badRequest().build();
        }
        return fileService.deleteTags(fileId, tagForm.getTags())
                .map(f -> ResponseEntity.noContent().build())
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean isNotValidFileId(String fileId) {
        return !fileId.matches("/(.|[\r\n])*|id:.*|(ns:[0-9]+(/.*)?)");
    }

    private boolean isNotValidTags(TagForm tagForm) {
        // parameter is null
        if (tagForm == null || tagForm.getTags() == null) {
            return true;
        }
        // some of tags are null or empty
        for (String tag : tagForm.getTags()) {
            if (tag == null || tag.isEmpty()) {
                return true;
            }
        }
        // the tags are duplicated
        return new HashSet<>(tagForm.getTags()).size() < tagForm.getTags().size();
    }

    private static HttpHeaders createLocationHeader(String locationHeaderLink) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", locationHeaderLink);
        headers.add("Access-Control-Expose-Headers", "Location");
        return headers;
    }

    private static ResponseEntity<EntityModel<FileInfo>> createResponseCreated(EntityModel<FileInfo> body, String locationHeaderLink) {
        HttpHeaders headers = createLocationHeader(locationHeaderLink);
        return new ResponseEntity<EntityModel<FileInfo>>(body, headers, HttpStatus.CREATED);
    }

}
