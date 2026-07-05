package com.demo.offline_upi.controller;

import com.demo.offline_upi.crypto.ServerKeyHolder;
import com.demo.offline_upi.dto.AccountBalanceDto;
import com.demo.offline_upi.dto.InboundPacketRequest;
import com.demo.offline_upi.dto.TransactionStatusResponse;
import com.demo.offline_upi.exception.DecryptionFailedException;
import com.demo.offline_upi.exception.DuplicatePacketException;
import com.demo.offline_upi.exception.TransactionReplayedException;
import com.demo.offline_upi.model.Transaction;
import com.demo.offline_upi.repository.AccountRepository;
import com.demo.offline_upi.repository.TransactionRepository;
import com.demo.offline_upi.service.BridgeIngestionService;
import com.demo.offline_upi.service.DemoService;
import com.demo.offline_upi.service.MeshSimulatorService;
import com.demo.offline_upi.service.VirtualDevice;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * REST Endpoint controller for the UPI Offline Mesh.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ServerKeyHolder keyHolder;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private TransactionRepository transactions;

    @Autowired
    private MeshSimulatorService simulator;

    @Autowired
    private DemoService demo;

    @Autowired
    private BridgeIngestionService bridge;

    @GetMapping("/server-key")
    public ResponseEntity<Map<String, String>> getServerKey() {
        return ResponseEntity.ok(Map.of("publicKey", keyHolder.getPublicKeyBase64()));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountBalanceDto>> getAccounts() {
        List<AccountBalanceDto> dtos = accounts.findAll().stream()
                .map(acc -> new AccountBalanceDto(acc.getVpa(), acc.getHolderName(), acc.getBalance()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions() {
        return ResponseEntity.ok(transactions.findTop20ByOrderByIdDesc());
    }

    @PostMapping("/bridge/ingest")
    public ResponseEntity<TransactionStatusResponse> ingestPacket(
            @RequestHeader(value = "X-Bridge-Node-Id") String bridgeNodeId,
            @Valid @RequestBody InboundPacketRequest request) {
        TransactionStatusResponse response = bridge.ingest(request, bridgeNodeId, 0);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mesh/reset")
    public ResponseEntity<Map<String, String>> resetMesh() {
        simulator.resetNetwork();
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/mesh/inject")
    public ResponseEntity<Map<String, String>> injectMesh(@RequestBody Map<String, Object> payload) throws Exception {
        String sender = (String) payload.get("sender");
        String receiver = (String) payload.get("receiver");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String pin = (String) payload.get("pin");
        String sourceDevice = (String) payload.get("sourceDevice");
        int ttl = Integer.parseInt(payload.get("ttl").toString());

        InboundPacketRequest packet = demo.createSignedTransaction(sender, receiver, amount, pin, ttl);
        simulator.injectPacket(sourceDevice, packet);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "packetId", packet.getPacketId()
        ));
    }

    @PostMapping("/mesh/gossip")
    public ResponseEntity<Map<String, String>> gossipMesh() {
        simulator.gossip();
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @GetMapping("/mesh/devices")
    public ResponseEntity<Collection<VirtualDevice>> getDevices() {
        return ResponseEntity.ok(simulator.getDevices());
    }

    @PostMapping("/mesh/flush")
    public ResponseEntity<List<Map<String, Object>>> meshFlush() {
        List<MeshSimulatorService.UploadRecord> uploads = simulator.getUploads();
        List<Map<String, Object>> results = new ArrayList<>();

        // Process uploads concurrently to simulate simultaneous network ingestion!
        uploads.parallelStream().forEach(up -> {
            String outcome;
            String reason = "";
            long txId = -1;
            try {
                TransactionStatusResponse r = bridge.ingest(up.packet(), up.bridgeNodeId(), 5 - up.packet().getTtl());
                outcome = r.getOutcome();
                if (r.getTransactionId() != null) {
                    txId = r.getTransactionId();
                }
            } catch (DuplicatePacketException e) {
                outcome = "DUPLICATE_DROPPED";
                reason = e.getMessage();
            } catch (DecryptionFailedException | TransactionReplayedException | IllegalArgumentException e) {
                outcome = "INVALID";
                reason = e.getMessage();
            } catch (Exception e) {
                outcome = "INVALID";
                reason = "internal_error: " + e.getMessage();
            }
            synchronized (results) {
                results.add(Map.of(
                        "bridgeNode", up.bridgeNodeId(),
                        "packetId", up.packet().getPacketId().substring(0, 8),
                        "outcome", outcome,
                        "reason", reason,
                        "transactionId", txId
                ));
            }
        });

        // After flushing, clear the active upload queues in the simulation
        simulator.clearUploads();
        return ResponseEntity.ok(results);
    }
}
