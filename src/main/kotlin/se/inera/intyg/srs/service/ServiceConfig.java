package se.inera.intyg.srs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("se.inera.intyg.srs.service, se.inera.intyg.src.customer")
public class ServiceConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public GetSRSInformationResponderImpl getSRSInformationResponder() {
        return new GetSRSInformationResponderImpl();
    }

}
