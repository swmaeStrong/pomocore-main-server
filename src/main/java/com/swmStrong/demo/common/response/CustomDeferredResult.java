package com.swmStrong.demo.common.response;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;

@Slf4j
@Getter
public class CustomDeferredResult<T> {
    
    private static final long DEFAULT_TIMEOUT_MS = 30000L; // 30초 기본 타임아웃
    
    private final DeferredResult<T> deferredResult;
    private final String identifier;
    
    public CustomDeferredResult(String identifier) {
        this(identifier, DEFAULT_TIMEOUT_MS);
    }
    
    public CustomDeferredResult(String identifier, long timeoutMs) {
        this.identifier = identifier;
        this.deferredResult = new DeferredResult<>(timeoutMs);
        
        this.deferredResult.onTimeout(() -> {
            log.debug("DeferredResult timeout for: {}", identifier);
            this.deferredResult.setResult(null);
        });
        
        this.deferredResult.onCompletion(() -> 
            log.debug("DeferredResult completed for: {}", identifier)
        );
        
        this.deferredResult.onError(throwable -> 
            log.error("DeferredResult error for: {}", identifier, throwable)
        );
    }
    
    public boolean setResult(T result) {
        return deferredResult.setResult(result);
    }
    
    public void onTimeout(Runnable callback) {
        deferredResult.onTimeout(callback);
    }
    
    public void onCompletion(Runnable callback) {
        deferredResult.onCompletion(callback);
    }
    
    public void onError(Consumer<Throwable> callback) {
        deferredResult.onError(callback);
    }
    
    public <R> DeferredResult<R> map(java.util.function.Function<T, R> mapper) {
        DeferredResult<R> mappedResult = new DeferredResult<>(DEFAULT_TIMEOUT_MS);

        deferredResult.setResultHandler(result -> {
            if (result != null) {
                mappedResult.setResult(mapper.apply((T) result));
            } else {
                mappedResult.setResult(null);
            }
        });

        deferredResult.onTimeout(() -> mappedResult.setResult(null));

        deferredResult.onError(mappedResult::setErrorResult);
        
        return mappedResult;
    }
}