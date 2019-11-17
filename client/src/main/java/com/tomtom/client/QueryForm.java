package com.tomtom.client;

import io.reactivex.Flowable;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.TextListener;

class QueryForm extends JFrame {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final ServiceClient serviceClient;
    private static final Logger LOGGER = LogManager.getLogger(QueryForm.class);

    QueryForm(final CloseableHttpAsyncClient client) {
        serviceClient = new ServiceClient(client);

        final TextField query = new TextField();
        query.setBounds(5, 15, 420, 25);
        add(query);

        final JList<String> results = new JList<>(model);
        results.setBounds(5, 40, 420, 210);
        add(results);

        setSize(450, 300);
        setLayout(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Kody pocztowe");

        serviceClient.findAddressByCode(queryCodeStream(query), 10)
                .subscribe(items -> {
                    LOGGER.info("GOT RESULTS {}", items.size());
                    SwingUtilities.invokeLater(() -> {
                        model.clear();
                        items.forEach(item -> model.addElement(item.toString()));
                    });
                }, err -> {
                    LOGGER.error("Got ERROR : {} <{}> - {}", err.getMessage(), err.getClass().getName(), err.getStackTrace());
                }, () -> LOGGER.info("all done!"));

        LOGGER.info("STARTED!");
    }

    private Flowable<String> queryCodeStream(final TextField textField) {

        // convert text changed events to a stream of events
        final TextListener listener = evt -> {
            final String queryText = textField.getText();
            LOGGER.info("new query {}", queryText);
        };

        textField.addTextListener(listener);

        return Flowable.never();

    }
}