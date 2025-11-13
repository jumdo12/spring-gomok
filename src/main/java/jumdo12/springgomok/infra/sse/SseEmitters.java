package jumdo12.springgomok.infra.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitters {

    private final Map<SseConnectId, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter add(Long roomId, Long userId) {
        SseConnectId key = new SseConnectId(roomId, userId);
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onTimeout(() -> emitters.remove(key));
        emitter.onError(e -> emitters.remove(key));

        emitters.put(key, emitter);
        return emitter;
    }

    public void sendMove(Long roomId, Long userId, Object data) {
        SseEmitter emitter = getEmitter(roomId, userId);
        if (emitter != null) {
            send(emitter, "move", data);
        }
    }

    public void sendRoomUpdate(Long roomId, Long userId, Object data) {
        SseEmitter emitter = getEmitter(roomId, userId);
        if (emitter != null) {
            send(emitter, "room-update", data);
        }
    }

    public void sendGameEnd(Long roomId, Long userId, Object data) {
        SseEmitter emitter = getEmitter(roomId, userId);
        if (emitter != null) {
            send(emitter, "game-end", data);
        }
    }

    private SseEmitter getEmitter(Long roomId, Long userId) {
        SseConnectId key = new SseConnectId(roomId, userId);
        return emitters.get(key);
    }

    private void send(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
