package com.demo.offline_upi.service;

import com.demo.offline_upi.model.PaymentInstruction;
import com.demo.offline_upi.model.Transaction;

public interface SettlementService {
    Transaction settle(PaymentInstruction instruction, String packetHash, String bridgeNodeId, int hopCount);
    Transaction recordAuthFailure(PaymentInstruction instruction, String packetHash, String bridgeNodeId, int hopCount, String reason);
}