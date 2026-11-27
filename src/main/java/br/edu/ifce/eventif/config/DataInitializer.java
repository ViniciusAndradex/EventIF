package br.edu.ifce.eventif.config;

import br.edu.ifce.eventif.service.DemoDataService;
import br.edu.ifce.eventif.service.SeedService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataInitializer {
    @Bean
    Clock clock(@Value("${app.time-zone}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    ApplicationRunner initializeData(SeedService seedService, DemoDataService demoData,
            @Value("${DEMO_DATA:false}") boolean demoEnabled) {
        return args -> {
            seedService.initialize();
            if (demoEnabled)
                demoData.initialize();
        };
    }
}
