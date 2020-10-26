package de.alley.maverick.web;

import de.alley.maverick.protocol.FileInfo;
import de.alley.maverick.protocol.TagForm;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@ExtendWith({SpringExtension.class})
@SpringBootTest
@ActiveProfiles("elasticsearch")
public class FileRestControllerElasticsearchTest extends GeneralControllerTest {

    @Test
    @Order(0)
    public void testGetFileWithCorrectFileId() throws Exception {
        FileInfo correct = givenCorrectFileInfo();
        ResultActions result = getFile(correct.getId());
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(equalTo(correct.getId()))))
                .andExpect(jsonPath("$.name", is(equalTo(correct.getName()))))
                .andExpect(jsonPath("$.path", is(equalTo(correct.getPath()))))
                .andExpect(jsonPath("$.size", is(equalTo(correct.getSize().intValue()))));
    }

    @Test
    @Order(1)
    public void testGetFileWithNotValidFileId() throws Exception {
        FileInfo notValid = givenNotValidFileInfo();
        ResultActions result = getFile(notValid.getId());
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(2)
    public void testGetFileWithValidButUnknownFileId() throws Exception {
        FileInfo unknown = givenUnknownFileInfo();
        ResultActions result = getFile(unknown.getId());
        result
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    public void testResetTagsWithCorrectFileId() throws Exception {
        FileInfo correct = givenCorrectFileInfo();
        List<String> tagsInit = new ArrayList<>();
        tagsInit.add("tag1");
        tagsInit.add("tag2");
        tagsInit.add("tag3");

        TagForm tagFormInit = TagForm.builder().tags(tagsInit).build();
        log.info(String.format("tagsForm -> %s", tagFormInit));

        ResultActions resultInit = resetTags(correct.getId(), tagFormInit);
        resultInit
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(SLASH + correct.getId())))
                .andExpect(jsonPath("$.id", is(equalTo(correct.getId()))))
                .andExpect(jsonPath("$.name", is(equalTo(correct.getName()))))
                .andExpect(jsonPath("$.path", is(equalTo(correct.getPath()))))
                .andExpect(jsonPath("$.size", is(equalTo(correct.getSize().intValue()))))
                .andExpect(jsonPath("$.tags", hasSize(3)))
                .andExpect((jsonPath("$.tags", Matchers.containsInAnyOrder("tag1", "tag2", "tag3"))));

        List<String> tagsReseted = new ArrayList<>();
        tagsReseted.add("tag4");
        tagsReseted.add("tag5");
        tagsReseted.add("tag6");

        TagForm tagFormReseted = TagForm.builder().tags(tagsReseted).build();
        log.info(String.format("tagsForm -> %s", tagFormReseted));

        ResultActions resultReseted = resetTags(correct.getId(), tagFormReseted);
        resultReseted
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(SLASH + correct.getId())))
                .andExpect(jsonPath("$.id", is(equalTo(correct.getId()))))
                .andExpect(jsonPath("$.name", is(equalTo(correct.getName()))))
                .andExpect(jsonPath("$.path", is(equalTo(correct.getPath()))))
                .andExpect(jsonPath("$.size", is(equalTo(correct.getSize().intValue()))))
                .andExpect(jsonPath("$.tags", hasSize(3)))
                .andExpect((jsonPath("$.tags", Matchers.containsInAnyOrder("tag4", "tag5", "tag6"))));
    }

    @Test
    @Order(4)
    public void testResetTagsWithNotValidFileId() throws Exception {
        FileInfo notValid = givenNotValidFileInfo();
        ResultActions result = resetTags(notValid.getId(), givenRandomTagForm());
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void testResetTagsWithNotValidTags() throws Exception {
        FileInfo notValid = givenCorrectFileInfo();
        List<String> notValidTags = new ArrayList<>();
        notValidTags.add("");
        notValidTags.add("tag5");
        notValidTags.add("tag6");
        TagForm tagFormNotValid = TagForm.builder().tags(notValidTags).build();
        ResultActions result = resetTags(notValid.getId(), tagFormNotValid);
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void testResetTagsWithDuplicatedTags() throws Exception {
        FileInfo notValid = givenCorrectFileInfo();
        List<String> notValidTags = new ArrayList<>();
        notValidTags.add("tag");
        notValidTags.add("tag");
        notValidTags.add("tag");
        TagForm tagFormNotValid = TagForm.builder().tags(notValidTags).build();
        ResultActions result = resetTags(notValid.getId(), tagFormNotValid);
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void testResetTagsWithValidButUnknownFileId() throws Exception {
        FileInfo unknown = givenUnknownFileInfo();
        ResultActions result = resetTags(unknown.getId(), givenRandomTagForm());
        result
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    public void testMergeTagsWithCorrectFileId() throws Exception {
        FileInfo correct = givenCorrectFileInfo();
        List<String> tagsInit = new ArrayList<>();
        tagsInit.add("tag1");
        tagsInit.add("tag2");
        tagsInit.add("tag3");

        TagForm tagFormInit = TagForm.builder().tags(tagsInit).build();
        log.info(String.format("tagsForm -> %s", tagFormInit));

        ResultActions resultInit = resetTags(correct.getId(), tagFormInit);
        resultInit
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(SLASH + correct.getId())))
                .andExpect(jsonPath("$.id", is(equalTo(correct.getId()))))
                .andExpect(jsonPath("$.name", is(equalTo(correct.getName()))))
                .andExpect(jsonPath("$.path", is(equalTo(correct.getPath()))))
                .andExpect(jsonPath("$.size", is(equalTo(correct.getSize().intValue()))))
                .andExpect(jsonPath("$.tags", hasSize(3)))
                .andExpect((jsonPath("$.tags", Matchers.containsInAnyOrder("tag1", "tag2", "tag3"))));

        List<String> tagsMerged = new ArrayList<>();
        tagsMerged.add("tag1");
        tagsMerged.add("tag4");
        tagsMerged.add("tag5");

        TagForm tagFormMerged = TagForm.builder().tags(tagsMerged).build();
        log.info(String.format("tagsForm -> %s", tagFormMerged));

        ResultActions resultMerged = mergeTags(correct.getId(), tagFormMerged);
        resultMerged
                .andDo(print())
                .andExpect(status().isNoContent());

        ResultActions result = getFile(correct.getId());
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(equalTo(correct.getId()))))
                .andExpect(jsonPath("$.name", is(equalTo(correct.getName()))))
                .andExpect(jsonPath("$.path", is(equalTo(correct.getPath()))))
                .andExpect(jsonPath("$.size", is(equalTo(correct.getSize().intValue()))))
                .andExpect(jsonPath("$.tags", hasSize(5)))
                .andExpect((jsonPath("$.tags",
                        Matchers.containsInAnyOrder("tag1", "tag2", "tag3","tag4", "tag5"))));
    }

    @Test
    @Order(9)
    public void testMergeTagsWithNotValidFileId() throws Exception {
        FileInfo notValid = givenNotValidFileInfo();
        ResultActions result = mergeTags(notValid.getId(), givenRandomTagForm());
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    public void testMergeTagsWithNotValidTags() throws Exception {
        FileInfo notValid = givenCorrectFileInfo();
        List<String> notValidTags = new ArrayList<>();
        notValidTags.add("");
        notValidTags.add("tag5");
        notValidTags.add("tag6");
        TagForm tagFormNotValid = TagForm.builder().tags(notValidTags).build();
        ResultActions result = mergeTags(notValid.getId(), tagFormNotValid);
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void testMergeTagsWithDuplicatedTags() throws Exception {
        FileInfo notValid = givenCorrectFileInfo();
        List<String> notValidTags = new ArrayList<>();
        notValidTags.add("tag");
        notValidTags.add("tag");
        notValidTags.add("tag");
        TagForm tagFormNotValid = TagForm.builder().tags(notValidTags).build();
        ResultActions result = mergeTags(notValid.getId(), tagFormNotValid);
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(12)
    public void testMergeTagsWithValidButUnknownFileId() throws Exception {
        FileInfo unknown = givenUnknownFileInfo();
        ResultActions result = mergeTags(unknown.getId(), givenRandomTagForm());
        result
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    public void testDeleteTagsWithCorrectFileId() throws Exception {
        FileInfo correct = givenCorrectFileInfo();
        List<String> tagsInit = new ArrayList<>();
        tagsInit.add("tag1");
        tagsInit.add("tag2");
        tagsInit.add("tag3");

        TagForm tagFormInit = TagForm.builder().tags(tagsInit).build();
        log.info(String.format("tagsForm -> %s", tagFormInit));

        ResultActions resultInit = resetTags(correct.getId(), tagFormInit);
        resultInit
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(SLASH + correct.getId())))
                .andExpect(jsonPath("$.id", is(equalTo(correct.getId()))))
                .andExpect(jsonPath("$.name", is(equalTo(correct.getName()))))
                .andExpect(jsonPath("$.path", is(equalTo(correct.getPath()))))
                .andExpect(jsonPath("$.size", is(equalTo(correct.getSize().intValue()))))
                .andExpect(jsonPath("$.tags", hasSize(3)))
                .andExpect((jsonPath("$.tags", Matchers.containsInAnyOrder("tag1", "tag2", "tag3"))));

        List<String> tagsDeleted = new ArrayList<>();
        tagsDeleted.add("tag2");
        tagsDeleted.add("tag3");

        TagForm tagFormDeleted = TagForm.builder().tags(tagsDeleted).build();
        log.info(String.format("tagsForm -> %s", tagFormDeleted));

        ResultActions resultDeleted = deleteTags(correct.getId(), tagFormDeleted);
        resultDeleted
                .andDo(print())
                .andExpect(status().isNoContent());

        ResultActions result = getFile(correct.getId());
        result
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(equalTo(correct.getId()))))
                .andExpect(jsonPath("$.name", is(equalTo(correct.getName()))))
                .andExpect(jsonPath("$.path", is(equalTo(correct.getPath()))))
                .andExpect(jsonPath("$.size", is(equalTo(correct.getSize().intValue()))))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect((jsonPath("$.tags",
                        Matchers.containsInAnyOrder("tag1"))));
    }

    @Test
    @Order(14)
    public void testDeleteTagsWithNotValidFileId() throws Exception {
        FileInfo notValid = givenNotValidFileInfo();
        ResultActions result = deleteTags(notValid.getId(), givenRandomTagForm());
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(15)
    public void testDeleteTagsWithNotValidTags() throws Exception {
        FileInfo notValid = givenCorrectFileInfo();
        List<String> notValidTags = new ArrayList<>();
        notValidTags.add("");
        notValidTags.add("tag5");
        notValidTags.add("tag6");
        TagForm tagFormNotValid = TagForm.builder().tags(notValidTags).build();
        ResultActions result = deleteTags(notValid.getId(), tagFormNotValid);
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(16)
    public void testDeleteTagsWithDuplicatedTags() throws Exception {
        FileInfo notValid = givenCorrectFileInfo();
        List<String> notValidTags = new ArrayList<>();
        notValidTags.add("tag");
        notValidTags.add("tag");
        notValidTags.add("tag");
        TagForm tagFormNotValid = TagForm.builder().tags(notValidTags).build();
        ResultActions result = deleteTags(notValid.getId(), tagFormNotValid);
        result
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    public void testDeleteTagsWithValidButUnknownFileId() throws Exception {
        FileInfo unknown = givenUnknownFileInfo();
        ResultActions result = deleteTags(unknown.getId(), givenRandomTagForm());
        result
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(18)
    public void testSearchFileNotEmptyResult() throws Exception {
        ResultActions resultElasticsearch = searchFile("Transactions");
        resultElasticsearch
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)));
    }

    @Test
    @Order(19)
    public void testSearchFileEmptyResult() throws Exception {
        ResultActions resultElasticsearch = searchFile(faker.internet().uuid());
        resultElasticsearch
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

}
