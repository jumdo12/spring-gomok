package jumdo12.springgomok.presentation;

import jumdo12.springgomok.common.execption.BusinessException;
import jumdo12.springgomok.common.execption.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        log.warn("No resource found: {}", e.getMessage());
        return ResponseEntity.status(404)
                .body(new ErrorResponse("NOT_FOUND", "존재하지 않는 리소스입니다"));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(new ErrorResponse(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error", e);

        return ResponseEntity.status(500)
                .body(new ErrorResponse("SERVER_ERROR", "서버 오류가 발생했습니다"));
    }
}
