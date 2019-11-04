package com.tomtom.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.concurrent.FutureCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class ResponseHandler<T> extends CompletableFuture<List<T>> implements FutureCallback<HttpResponse> {

    private static final Logger LOGGER = LogManager.getLogger(ResponseHandler.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final JavaType typeReference;

    ResponseHandler(Class<T> target) {
        typeReference = MAPPER.getTypeFactory().constructCollectionLikeType(List.class, target);
    }

    @Override
    public void completed(final HttpResponse result) {
        try {
            final int statusCode = result.getStatusLine().getStatusCode();
            LOGGER.info("Got response {}", statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                final List<T> mappedResult = MAPPER.readValue(result.getEntity().getContent(), typeReference);
                LOGGER.info("Response contains {} {} entities", mappedResult.size(), typeReference.getContentType().getTypeName());
                complete(mappedResult);
            } else {
                throw new RuntimeException("Unexpected status code " + statusCode);
            }
        } catch (final Exception e) {
            LOGGER.error("Error processing result - {} <{}>", e.getMessage(), e.getClass().getName());
            completeExceptionally(e);
        }
    }

    @Override
    public void failed(final Exception ex) {
        LOGGER.error("Request failed {} <{}>", ex.getMessage(), ex.getClass().getName());
        completeExceptionally(ex);
    }

    @Override
    public void cancelled() {
        LOGGER.warn("Request cancelled");
        completeExceptionally(new CancellationException());
    }
}
