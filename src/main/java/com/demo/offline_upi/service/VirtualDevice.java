package com.demo.offline_upi.service;

import com.demo.offline_upi.dto.InboundPacketRequest;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simulated phone/device in the mesh network. Holds packets it has received.
 */
public class VirtualDevice {

    private final String deviceId;
    private final boolean hasInternet;
    private final Map<String, InboundPacketRequest> heldPackets = new ConcurrentHashMap<>();

    public VirtualDevice(String deviceId, boolean hasInternet) {
        this.deviceId = deviceId;
        this.hasInternet = hasInternet;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean hasInternet() {
        return hasInternet;
    }

    public void hold(InboundPacketRequest packet) {
        heldPackets.putIfAbsent(packet.getPacketId(), packet);
    }

    public Collection<InboundPacketRequest> getHeldPackets() {
        return heldPackets.values();
    }

    public boolean holds(String packetId) {
        return heldPackets.containsKey(packetId);
    }

    public int packetCount() {
        return heldPackets.size();
    }

    public void clear() {
        heldPackets.clear();
    }
}
