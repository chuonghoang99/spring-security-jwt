package com.chuong.training.configuration.locale;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
public class LocalResolver extends AcceptHeaderLocaleResolver implements WebMvcConfigurer {

    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    List<Locale> LOCALES = List.of(Locale.of("en"), Locale.of("vn"));

    @Override
    public Locale resolveLocale(HttpServletRequest request) {

        String languageHeader = request.getHeader(ACCEPT_LANGUAGE_HEADER);

        return StringUtils.hasLength(languageHeader) ?
                Locale.lookup(Locale.LanguageRange.parse(languageHeader),
                        LOCALES) :
                Locale.US;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource rs = new ResourceBundleMessageSource();
        rs.setBasename("i18n/messages");
        rs.setDefaultEncoding("UTF-8");
        rs.setUseCodeAsDefaultMessage(true);
        rs.setCacheSeconds(3600);
        return rs;
    }
}
