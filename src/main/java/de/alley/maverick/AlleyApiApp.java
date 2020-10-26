package de.alley.maverick;

import de.alley.maverick.services.StarterSynchronizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication(scanBasePackages = {"de.alley"})
@EnableAsync
@EnableScheduling
public class AlleyApiApp extends SpringBootServletInitializer implements CommandLineRunner {

    @Autowired
    private StarterSynchronizer starterSynchronizer;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AlleyApiApp.class);
    }

    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        SpringApplication.run(AlleyApiApp.class, args);
    }

    @Override
    public void run(String...args) throws Exception {
        int i = 0; boolean result = false;
        while (!result && i<3) {
            Thread.sleep(10000);
            i++;
            log.info(String.format("Attempt #%d", i));
            result = starterSynchronizer.checkElasticsearch();
        }
        if (result) {
            starterSynchronizer.syncFiles();
        }
    }

}
