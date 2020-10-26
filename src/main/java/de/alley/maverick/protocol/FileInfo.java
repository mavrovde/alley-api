package de.alley.maverick.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
public class FileInfo /*extends RepresentationModel<FileInfo> */{

    @NotBlank
    @Pattern(regexp = "/(.|[\r\n])*|id:.*|(ns:[0-9]+(/.*)?)")
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String path;

    @NotBlank
    @EqualsAndHashCode.Exclude
    private Long size;

    @NotNull
    @EqualsAndHashCode.Exclude
    private List<String> tags;

    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

}
