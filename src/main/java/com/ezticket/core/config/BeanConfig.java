package com.ezticket.core.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration // indicates this is a configuration class.
public class BeanConfig {

    @Bean
    public ModelMapper metodoQueCriaUmModelMapper() {
        return new ModelMapper();
    }

    @Bean
    public BCryptPasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
