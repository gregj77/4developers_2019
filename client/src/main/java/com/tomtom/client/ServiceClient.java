package com.tomtom.client;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class ServiceClient {
    private static final Logger LOGGER = LogManager.getLogger(ServiceClient.class);

    private final String SERVICE_URL = "http://localhost:8080";
    private final String QUERY_CODE_URL = "/code?codePart=";
    private final String ADDRESS_LOOKUP_URL = "/addresspoint/";
    private final HttpAsyncClient client;

    ServiceClient(final HttpAsyncClient client) {

        this.client = client;
    }

    CompletableFuture<List<CodeToAddress>> findAddressByCode(final String codePart, final int limit) {

        final CompletableFuture<List<CodeToAddress>> retValue = new CompletableFuture<>();

        lookupAddressCodes(codePart)
                .whenComplete((result, err) -> {

                    if (err != null) {
                        LOGGER.warn("Query '{}' - completed with error {} <{}>", codePart, err.getMessage(), err.getClass().getName());
                        retValue.completeExceptionally(err);
                        return;
                    }

                    LOGGER.info("Query '{}' - completed with {} value(s)", codePart, result.size());
                    int remaining = limit;

                    final List<CompletableFuture<List<CodeToAddress>>> codeToAddressQuery = new ArrayList<>();
                    for (final String completeCode : result) {
                        if (remaining-- <= 0) break;

                        LOGGER.info("{}) Query details '{}'", remaining, completeCode);
                        CompletableFuture<List<CodeToAddress>> byCode = getAddressForPostalCode(completeCode);
                        codeToAddressQuery.add(byCode);
                    }

                    CompletableFuture
                            .allOf(codeToAddressQuery.toArray(new CompletableFuture[0]))
                            .whenComplete(((aVoid, throwable) -> {
                                final List<CodeToAddress> content = codeToAddressQuery
                                    .stream()
                                    .flatMap(p -> {
                                        try {
                                            return p.get().stream();
                                        } catch (Exception e) {
                                            LOGGER.error("fetch code details completed with error : {} <{}>", e.getMessage(), e.getClass().getName());
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.toList());
                                LOGGER.info("query details -> {} item(s)", content.size());
                                retValue.complete(content);
                        }));
                });

        return retValue;
    }

    private CompletableFuture<List<String>> lookupAddressCodes(final String codePart) {

        final ResponseHandler<String> retValue = new ResponseHandler<>(String.class);
        final HttpGet request = new HttpGet(SERVICE_URL + QUERY_CODE_URL + codePart);
        client.execute(request, retValue);
        return retValue;
    }

    private CompletableFuture<List<CodeToAddress>> getAddressForPostalCode(final String postalCode) {
        final ResponseHandler<CodeToAddress> retValue = new ResponseHandler<>(CodeToAddress.class);
        final HttpGet request = new HttpGet(SERVICE_URL + ADDRESS_LOOKUP_URL  + postalCode);
        client.execute(request, retValue);
        return retValue;
    }
}
