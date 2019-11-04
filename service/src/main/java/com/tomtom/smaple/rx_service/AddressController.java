package com.tomtom.smaple.rx_service;

import com.opencsv.CSVReader;
import com.tomtom.smaple.dto.CodeToAddress;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AddressController {

    private MultiMap codeToAddress = new MultiValueMap();

    public AddressController() throws IOException {
        File csv = new File(getClass().getClassLoader().getResource("kody.csv").getFile());
        try (FileReader fileReader = new FileReader(csv)) {
            try (CSVReader csvReader = new CSVReader(fileReader, ';')) {
                csvReader.readNext();
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    codeToAddress.put(line[0], line[2] + ", " + line[1]);
                }
            }
        }
        System.out.println("contains " + codeToAddress.size());
    }

    @RequestMapping("/code")
    public ResponseEntity<List<String>> validCodes(@RequestParam(required = false, value = "codePart")String codePart) throws InterruptedException {
        if (null == codePart || codePart.length() == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Thread.sleep(50);

        List<String>result = (List<String>)codeToAddress
                .keySet()
                .stream()
                .map(p -> (String) p)
                .filter(p -> ((String) p).startsWith(codePart))
                .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/addresspoint/{code}")
    public ResponseEntity<List<CodeToAddress>> codeToAddressMapping(@PathVariable(value = "code") String code) throws InterruptedException {
        if (!codeToAddress.containsKey(code)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Thread.sleep(100);

        List<String> addressPoints = (List<String>)codeToAddress.get(code);
        List<CodeToAddress> result = addressPoints.stream().map(p -> new CodeToAddress(code, p)).collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
