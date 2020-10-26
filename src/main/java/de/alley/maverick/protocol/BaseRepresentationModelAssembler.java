package de.alley.maverick.protocol;

import java.lang.reflect.Field;

import de.alley.maverick.web.FileRestController;
import org.springframework.core.GenericTypeResolver;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.util.ReflectionUtils;

public class BaseRepresentationModelAssembler<T> implements SimpleRepresentationModelAssembler<T> {

    private final Class<?> controllerClass;
    private final LinkRelationProvider relProvider;
    private final Class<?> resourceType;
    private String basePath;

    public BaseRepresentationModelAssembler() {
        this(FileRestController.class, new EvoInflectorLinkRelationProvider());
    }

    public BaseRepresentationModelAssembler(Class<?> controllerClass, LinkRelationProvider relProvider) {
        this.basePath = "";
        this.controllerClass = controllerClass;
        this.relProvider = relProvider;
        this.resourceType = GenericTypeResolver.resolveTypeArgument(this.getClass(),
                BaseRepresentationModelAssembler.class);
    }

    public BaseRepresentationModelAssembler(Class<?> controllerClass) {
        this(controllerClass, new EvoInflectorLinkRelationProvider());
    }

    public void addLinks(EntityModel<T> resource) {
        resource.add(this.getCollectionLinkBuilder().slash(this.getId(resource)).withSelfRel());
        resource.add(this.getCollectionLinkBuilder().withRel(this.relProvider.getCollectionResourceRelFor(this.resourceType)));
    }

    private Object getId(EntityModel<T> resource) {
        Field id = ReflectionUtils.findField(this.resourceType, "id");
        ReflectionUtils.makeAccessible(id);
        return ReflectionUtils.getField(id, resource.getContent());
    }

    public void addLinks(CollectionModel<EntityModel<T>> resources) {
        resources.add(this.getCollectionLinkBuilder().withSelfRel());
    }

    protected LinkBuilder getCollectionLinkBuilder() {
        WebMvcLinkBuilder linkBuilder = WebMvcLinkBuilder.linkTo(this.controllerClass);
        String[] var2 = (this.getPrefix() + this.relProvider.getCollectionResourceRelFor(this.resourceType)).split("/");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String pathComponent = var2[var4];
            if (!pathComponent.isEmpty()) {
                linkBuilder = (WebMvcLinkBuilder)linkBuilder.slash(pathComponent);
            }
        }

        return linkBuilder;
    }

    private String getPrefix() {
        return this.getBasePath().isEmpty() ? "" : this.getBasePath() + "/";
    }

    public LinkRelationProvider getRelProvider() {
        return this.relProvider;
    }

    public Class<?> getResourceType() {
        return this.resourceType;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }
}
