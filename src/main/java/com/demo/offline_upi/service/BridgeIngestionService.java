package com.demo.offline_upi.service;

import com.demo.offline_upi.dto.InboundPacketRequest;
import com.demo.offline_upi.dto.TransactionStatusResponse;

/**
 * Interface defining bridge node transaction ingestion.
 */
public interface BridgeIngestionService {
    TransactionStatusResponse ingest(InboundPacketRequest packet, String bridgeNodeId, int hopCount);
}
