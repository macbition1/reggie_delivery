package com.chun.reggie.config;

import com.chun.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    /*
    * map static resource
    *but we can create a springboot application and add the resource in static part
    * */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        super.addResourceHandlers(registry);
        /*map webpage address to front and backend
        * classpath is resource
        * */
        log.info("Starting to map!!!");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }


    /**
     * Extend message converter of Spring MVC frame
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("Message Converter!");

        //create a new message converter
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

        //create object converter, make it possible to translate java object to json in base layer
        messageConverter.setObjectMapper(new JacksonObjectMapper());

        //combine it with mvc message converter, and use it priority
        converters.add(0,messageConverter);

    }
}
