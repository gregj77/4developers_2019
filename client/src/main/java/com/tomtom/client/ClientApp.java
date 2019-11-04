package com.tomtom.client;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.concurrent.ExecutionException;

public class ClientApp {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        new QueryForm(client);
    }
}
