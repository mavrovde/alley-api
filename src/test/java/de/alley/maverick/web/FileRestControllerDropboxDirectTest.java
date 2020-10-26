package de.alley.maverick.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith({SpringExtension.class})
@SpringBootTest
@ActiveProfiles("dropbox")
public class FileRestControllerDropboxDirectTest extends GeneralControllerTest {

    @Test
    @Order(0)
    public void testSearchFileNotEmptyResult() throws Exception {
        Thread.sleep(5000);
        ResultActions resultDropbox = searchFile("Transactions");
        resultDropbox
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)));
    }

    @Test
    @Order(1)
    public void testSearchFileEmptyResult() throws Exception {
        Thread.sleep(5000);
        ResultActions resultDropbox = searchFile(faker.internet().uuid());
        resultDropbox
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
