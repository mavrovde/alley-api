package de.alley.maverick.protocol;

import de.alley.maverick.web.FileRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FileInfoModelAssembler extends BaseRepresentationModelAssembler<FileInfo> {

    FileInfoModelAssembler() {
    }

    @Override
    public void addLinks(EntityModel<FileInfo> resource) {
        //super.addLinks(resource);
        resource.add(linkTo(methodOn(FileRestController.class)
                .getFileById(Objects.requireNonNull(resource.getContent()).getId())).withSelfRel())
        .add(linkTo(methodOn(FileRestController.class).resetTags(
                resource.getContent().getId(), TagForm.builder().tags(resource.getContent().getTags()).build()))
                .withRel("resetTags"))
        .add(linkTo(methodOn(FileRestController.class).mergeTags(
                resource.getContent().getId(), TagForm.builder().tags(resource.getContent().getTags()).build()))
                .withRel("mergeTags"))
        .add(linkTo(methodOn(FileRestController.class).deleteTags(
                resource.getContent().getId(), TagForm.builder().tags(resource.getContent().getTags()).build()))
                .withRel("deleteTags"));
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<FileInfo>> resources) {
        //super.addLinks(resources);
    }

}