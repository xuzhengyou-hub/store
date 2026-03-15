package com.yourname.mall.exception;

import com.yourname.mall.common.Result;
import com.yourname.mall.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.fail(ResultCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() == null
            ? "鍙傛暟鏍￠獙澶辫触"
            : ex.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return Result.fail(ResultCode.SERVER_ERROR, "鏈嶅姟鍣ㄥ唴閮ㄩ敊璇?);
    }
}
