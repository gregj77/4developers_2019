package com.tomtom.client;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.TextEvent;

class QueryForm extends JFrame {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final ServiceClient serviceClient;
    private static final Logger LOGGER = LogManager.getLogger(QueryForm.class);

    QueryForm(CloseableHttpAsyncClient client) {
        serviceClient = new ServiceClient(client);

        TextField query = new TextField();
        query.setBounds(5, 15, 420, 25);
        add(query);

        JList<String> results = new JList<>(model);
        results.setBounds(5, 40, 420, 210);
        add(results);

        setSize(450, 300);
        setLayout(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Kody pocztowe");

        query.addTextListener(this::onNewTextEntered);
        LOGGER.info("app started!");
    }

    private void onNewTextEntered(final TextEvent evt) {
        final String queryText = ((TextField) evt.getSource()).getText();
        serviceClient.findAddressByCode(queryText, 10).whenComplete((addresses, err) -> {
            if (err != null) {
                LOGGER.error("Got error while querying '{}' - {} <{}>", queryText, err.getMessage(), err.getClass().getName());
            } else {
                SwingUtilities.invokeLater(() -> {
                    LOGGER.info("Got back results for '{}' -> {}", queryText, addresses.size());
                    model.clear();
                    addresses.forEach(a -> model.addElement(a.toString()));
                });
            }
        });
    }
}