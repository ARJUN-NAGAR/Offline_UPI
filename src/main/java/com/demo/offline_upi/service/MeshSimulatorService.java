package com.demo.offline_upi.service;

import com.demo.offline_upi.dto.InboundPacketRequest;

import java.util.Collection;
import java.util.List;

/**
 * Interface defining operations for the simulated multihop BLE mesh simulator.
 */
public interface MeshSimulatorService {
    void init();
    void injectPacket(String sourceDeviceId, InboundPacketRequest packet);
    void gossip();
    Collection<VirtualDevice> getDevices();
    VirtualDevice getDevice(String deviceId);
    List<UploadRecord> getUploads();
    void clearUploads();
    void resetNetwork();

    record UploadRecord(String bridgeNodeId, InboundPacketRequest packet) {}
}
