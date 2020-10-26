package de.alley.maverick.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import de.alley.maverick.protocol.FileInfo;
import de.alley.maverick.protocol.TagForm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(initializers = {GeneralControllerTest.Initializer.class})
@ExtendWith({SpringExtension.class})
@SpringBootTest
public abstract class GeneralControllerTest {

    private static final String ELASTIC_SEARCH_DOCKER = "docker.elastic.co/elasticsearch/elasticsearch:7.8.0";
    private static final String CLUSTER_NAME = "cluster.name";
    private static final String ELASTIC_SEARCH = "elasticsearch";
    private static final int ELASTICSEARCH_DEFAULT_PORT = 9200;

    protected static final String SLASH = "/";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    protected final Faker faker = new Faker();

    public MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Container
    private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER =
            new ElasticsearchContainer(ELASTIC_SEARCH_DOCKER)
                    .withExposedPorts(ELASTICSEARCH_DEFAULT_PORT)
                    .withEnv(CLUSTER_NAME, ELASTIC_SEARCH)
                    .withEnv("discovery.type", "single-node")
                    .waitingFor(Wait.forHttp("/").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(3)));

    static {
        ELASTICSEARCH_CONTAINER.start();
        log.info("DOCKER-TEST: host  ->" + ELASTICSEARCH_CONTAINER.getContainerIpAddress());
        log.info("DOCKER-TEST: host2 ->" + ELASTICSEARCH_CONTAINER.getHttpHostAddress());
        log.info("DOCKER-TEST: port  ->" + ELASTICSEARCH_CONTAINER.getMappedPort(ELASTICSEARCH_DEFAULT_PORT));
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "elasticsearch.host=" + ELASTICSEARCH_CONTAINER.getContainerIpAddress(),
                    "elasticsearch.port=" + ELASTICSEARCH_CONTAINER.getMappedPort(ELASTICSEARCH_DEFAULT_PORT)
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    // service methods

    protected TagForm givenRandomTagForm() {
        List<String> tagsRandom = new ArrayList<>();
        tagsRandom.add(faker.internet().uuid());
        tagsRandom.add(faker.internet().uuid());
        tagsRandom.add(faker.internet().uuid());
        return TagForm.builder().tags(tagsRandom).build();
    }

    protected FileInfo givenCorrectFileInfo() {
        return FileInfo.builder()
                .id("id:K59R_zjubxAAAAAAAAAAFw")
                .name("Transactions12")
                .path("/apps/monefy/database/transactions12")
                .size(440L)
                .build();
    }

    protected FileInfo givenNotValidFileInfo() {
        return FileInfo.builder()
                .id(faker.idNumber().toString())
                .name("Transactions12")
                .path("/apps/monefy/database/transactions12")
                .size(440L)
                .build();
    }

    protected FileInfo givenUnknownFileInfo() {
        return FileInfo.builder()
                .id("id:K59R_zjubxAAAAAAAAAAFv")
                .name("Transactions12")
                .path("/apps/monefy/database/transactions12")
                .size(440L)
                .build();
    }

    protected ResultActions searchFile(String fileName) throws Exception {
        return mockMvc.perform(get(SLASH + "search")
                .param("fileName", fileName)
                .contentType(MediaType.APPLICATION_JSON)
        );
    }

    protected ResultActions getFile(String fileId) throws Exception {
        return mockMvc.perform(get(SLASH + fileId)
                .contentType(MediaType.APPLICATION_JSON)
        );
    }

    protected ResultActions resetTags(String fileId, TagForm tagForm) throws Exception {
        return mockMvc.perform(post(SLASH + fileId + SLASH + "tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(tagForm))
        );
    }

    protected ResultActions mergeTags(String fileId, TagForm tagForm) throws Exception {
        return mockMvc.perform(put(SLASH + fileId + SLASH + "tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(tagForm))
        );
    }

    protected ResultActions deleteTags(String fileId, TagForm tagForm) throws Exception {
        return mockMvc.perform(delete(SLASH + fileId + SLASH + "tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(tagForm))
        );
    }

    protected String asJson(Object object) throws JsonProcessingException {
        return this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(object);
    }

}
