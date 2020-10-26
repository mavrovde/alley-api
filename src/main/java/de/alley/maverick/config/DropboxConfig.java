package de.alley.maverick.config;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
public class DropboxConfig {

    @Value("${dropbox.accessToken}")
    private String accessToken;

    @Autowired
    private Environment env;

    @Bean
    public DbxClientV2 dpClient() {
        String access =  env.getProperty("DROPBOX_TOKEN", accessToken);
        log.info(String.format("access token for dropbox is %s", access));
        DbxRequestConfig config = DbxRequestConfig.newBuilder("alley-test").build();
        return new DbxClientV2(config, access);
    }

}
