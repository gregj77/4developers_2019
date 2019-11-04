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

        final Flowable<List<CodeToAddress>> mappedAddressCodeStream = codePartStream
                .debounce(500, TimeUnit.MILLISECONDS, Schedulers.computation())
                .filter(f -> f.length() >= 2)
                .doOnNext(p -> LOGGER.info("querying - '{}'", p))
                .map(lookupCode -> {

                    final Flowable<String> matchedCodesStream = lookupAddressCodes(lookupCode)
                            .onErrorReturnItem("lookup error for: " + lookupCode)
                            .take(10);

                    final Flowable<Flowable<List<CodeToAddress>>> fullAddressPointsStream = matchedCodesStream
                            .map(code -> getAddressForPostalCode(code).onErrorReturnItem(new ArrayList<>()));

                    return Flowable
                            .merge(fullAddressPointsStream)
                            .reduce(new ArrayList<CodeToAddress>(), (buffer, item) -> {
                                buffer.addAll(item);
                                return buffer;
                            });
                })
                .flatMapSingle(p -> p);

        return mappedAddressCodeStream;

    }

    private Flowable<String> lookupAddressCodes(final String codePart) {

        return Flowable.<String>create(observer -> {
            final ResponseHandler<String> retValue = new ResponseHandler<>(String.class);
            final HttpGet request = new HttpGet(SERVICE_URL + QUERY_CODE_URL + codePart);
            client.execute(request, retValue);

            retValue.whenComplete((items, error) -> {

                if (error != null) {
                    observer.onError(error);
                    return;
                }

                items.forEach(observer::onNext);
                observer.onComplete();
            });


        }, BackpressureStrategy.BUFFER);
    }

    private Flowable<List<CodeToAddress>> getAddressForPostalCode(final String postalCode) {
        return Flowable.<List<CodeToAddress>>create(observer -> {
            final ResponseHandler<CodeToAddress> retValue = new ResponseHandler<>(CodeToAddress.class);
            final HttpGet request = new HttpGet(SERVICE_URL + ADDRESS_LOOKUP_URL + postalCode);
            client.execute(request, retValue);
            retValue.whenComplete((result, error) -> {
                if (null != error) {
                    observer.onError(error);
                    return;
                }

                observer.onNext(result);
                observer.onComplete();
            });
        }, BackpressureStrategy.BUFFER);
    }
}
