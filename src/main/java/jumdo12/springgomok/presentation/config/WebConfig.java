package jumdo12.springgomok.presentation.config;

import jumdo12.springgomok.presentation.resolver.HttpLoginUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final HttpLoginUserArgumentResolver httpLoginUserArgumentResolver;

    public WebConfig(HttpLoginUserArgumentResolver httpLoginUserArgumentResolver) {
        this.httpLoginUserArgumentResolver = httpLoginUserArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(httpLoginUserArgumentResolver);
    }
}
