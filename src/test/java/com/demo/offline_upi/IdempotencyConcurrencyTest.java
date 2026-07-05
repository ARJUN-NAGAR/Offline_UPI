package com.demo.offline_upi;

import com.demo.offline_upi.dto.InboundPacketRequest;
import com.demo.offline_upi.dto.TransactionStatusResponse;
import com.demo.offline_upi.exception.DuplicatePacketException;
import com.demo.offline_upi.repository.TransactionRepository;
import com.demo.offline_upi.service.BridgeIngestionService;
import com.demo.offline_upi.service.DemoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class IdempotencyConcurrencyTest {

    @Autowired
    private DemoService demo;

    @Autowired
    private BridgeIngestionService bridge;

    @Autowired
    private TransactionRepository transactions;

    @BeforeEach
    public void setup() {
        transactions.deleteAll();
    }

    @Test
    public void testConcurrentUploadOfSamePacket() throws Exception {
        // Create 1 valid signed packet
        InboundPacketRequest p = demo.createSignedTransaction(
                "alice@demo", "bob@demo", new BigDecimal("10.00"), "1234", 5);

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        List<Object> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait for start signal
                    TransactionStatusResponse r = bridge.ingest(p, "Bridge_Node_" + index, 1);
                    results.add(r);
                } catch (Exception e) {
                    results.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // FIRE!
        endLatch.await();
        executor.shutdown();

        // Audit the outcomes
        long settled = results.stream()
                .filter(r -> r instanceof TransactionStatusResponse && "SETTLED".equals(((TransactionStatusResponse) r).getOutcome()))
                .count();

        long duplicates = results.stream()
                .filter(r -> r instanceof DuplicatePacketException || (r instanceof Throwable && ((Throwable) r).getCause() instanceof DuplicatePacketException))
                .count();

        assertEquals(1, settled, "Exactly one parallel thread should settle the transaction");
        assertEquals(threads - 1, duplicates, "All other parallel threads should fail with DuplicatePacketException");
        assertEquals(1, transactions.count(), "DB must only contain exactly 1 transaction record");
    }
}
