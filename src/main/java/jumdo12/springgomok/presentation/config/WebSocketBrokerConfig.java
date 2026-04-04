package jumdo12.springgomok.presentation.config;

import jumdo12.springgomok.presentation.interceptor.SessionChannelInterceptor;
import jumdo12.springgomok.presentation.interceptor.StompPrincipalHandshakeInterceptor;
import jumdo12.springgomok.presentation.resolver.HttpLoginUserArgumentResolver;
import jumdo12.springgomok.presentation.resolver.StompLoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    private final SessionChannelInterceptor sessionChannelInterceptor;
    private final StompPrincipalHandshakeInterceptor stompPrincipalHandshakeInterceptor;
    private final StompLoginUserArgumentResolver stompLoginUserArgumentResolver;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(stompPrincipalHandshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(sessionChannelInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(stompLoginUserArgumentResolver);
    }
}
