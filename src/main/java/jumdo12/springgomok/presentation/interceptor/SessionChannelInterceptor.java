package jumdo12.springgomok.presentation.interceptor;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorCode;
import jumdo12.springgomok.presentation.resolver.StompPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class SessionChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if(isConnect(command)) {
            authenticateUser(accessor);
        }

        return message;
    }

    private boolean isConnect(StompCommand stompCommand) {
        return StompCommand.CONNECT.equals(stompCommand);
    }

    private void authenticateUser(StompHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get(StompPrincipalHandshakeInterceptor.USER_ID);

        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        accessor.setUser(new StompPrincipal(userId));
    }
}
