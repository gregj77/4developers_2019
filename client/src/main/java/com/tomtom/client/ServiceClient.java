package com.tomtom.client;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class ServiceClient {
    private static final Logger LOGGER = LogManager.getLogger(ServiceClient.class);

    private final String SERVICE_URL = "http://localhost:8080";
    private final String QUERY_CODE_URL = "/code?codePart=";
    private final String ADDRESS_LOOKUP_URL = "/addresspoint/";
    private final HttpAsyncClient client;

    ServiceClient(final HttpAsyncClient client) {

        this.client = client;
    }

    Flowable<List<CodeToAddress>> findAddressByCode(final Flowable<String> codePartStream, final int limit) {
        // 1. lookup codes

        // 2. foreach code get matched addresses

        // 3. merge results into single collection

        // 4. return observable collection

        /// extras:
        // filtering, delayed typing, error handling

        final Flowable<List<CodeToAddress>> mappedAddressCodeStream = Flowable.never();

        return mappedAddressCodeStream;

    }

    private Flowable<String> lookupAddressCodes(final String codePart) {

        final ResponseHandler<String> retValue = new ResponseHandler<>(String.class);
        final HttpGet request = new HttpGet(SERVICE_URL + QUERY_CODE_URL + codePart);
        client.execute(request, retValue);

        retValue.whenComplete((items, error) -> {

        });

        return Flowable.error(new RuntimeException("lookupAddressCodes - not implemented yet!"));
    }

    private Flowable<List<CodeToAddress>> getAddressForPostalCode(final String postalCode) {
        final ResponseHandler<CodeToAddress> retValue = new ResponseHandler<>(CodeToAddress.class);
        final HttpGet request = new HttpGet(SERVICE_URL + ADDRESS_LOOKUP_URL + postalCode);
        client.execute(request, retValue);
        retValue.whenComplete((result, error) -> {

        });

        return Flowable.error(new RuntimeException("getAddressForPostalCode - not implemented yet!"));
    }
}
