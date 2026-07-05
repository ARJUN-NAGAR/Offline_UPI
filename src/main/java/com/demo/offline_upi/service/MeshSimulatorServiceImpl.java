package com.demo.offline_upi.service;

import com.demo.offline_upi.dto.InboundPacketRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service implementation of multihop BLE gossip simulation.
 * Devices communicate via: A <-> B <-> C <-> D (Bridge) and C <-> E (Bridge).
 */
@Service
public class MeshSimulatorServiceImpl implements MeshSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(MeshSimulatorServiceImpl.class);

    private final Map<String, VirtualDevice> devices = new ConcurrentHashMap<>();
    private final List<UploadRecord> activeUploads = new CopyOnWriteArrayList<>();

    @PostConstruct
    @Override
    public void init() {
        // Create 5 virtual devices.
        // A, B, C are completely offline.
        // D, E are internet-enabled bridges.
        devices.put("Device_A", new VirtualDevice("Device_A", false));
        devices.put("Device_B", new VirtualDevice("Device_B", false));
        devices.put("Device_C", new VirtualDevice("Device_C", false));
        devices.put("Device_D", new VirtualDevice("Device_D", true));
        devices.put("Device_E", new VirtualDevice("Device_E", true));
        log.info("Mesh simulator initialized with 5 devices (A, B, C offline; D, E bridges)");
    }

    @Override
    public void injectPacket(String sourceDeviceId, InboundPacketRequest packet) {
        VirtualDevice dev = devices.get(sourceDeviceId);
        if (dev != null) {
            dev.hold(packet);
            log.info("Injected packet {} into source device {}", packet.getPacketId().substring(0, 8), sourceDeviceId);
            // If the source device itself has internet, it uploads immediately
            if (dev.hasInternet()) {
                activeUploads.add(new UploadRecord(dev.getDeviceId(), packet));
            }
        }
    }

    /**
     * Simulates one tick of BLE proximity/gossip.
     * In each tick, if device X has packets, it shares them with its neighbors.
     * Sharing decrements TTL by 1. If TTL hits 0, it won't propagate further.
     */
    @Override
    public synchronized void gossip() {
        // We will perform updates on a snapshot to avoid concurrent modification issues during gossip
        Map<String, Set<InboundPacketRequest>> toAdd = new HashMap<>();
        for (String id : devices.keySet()) {
            toAdd.put(id, new HashSet<>());
        }

        // Define topology
        // A is connected to B
        // B is connected to A, C
        // C is connected to B, D, E
        // D (Bridge) is connected to C
        // E (Bridge) is connected to C
        share("Device_A", "Device_B", toAdd);
        share("Device_B", "Device_A", toAdd);
        share("Device_B", "Device_C", toAdd);
        share("Device_C", "Device_B", toAdd);
        share("Device_C", "Device_D", toAdd);
        share("Device_C", "Device_E", toAdd);
        share("Device_D", "Device_C", toAdd);
        share("Device_E", "Device_C", toAdd);

        // Apply shared packets
        toAdd.forEach((devId, packets) -> {
            VirtualDevice dev = devices.get(devId);
            for (InboundPacketRequest p : packets) {
                if (!dev.holds(p.getPacketId())) {
                    dev.hold(p);
                    log.info("Packet {} gossiped to {}", p.getPacketId().substring(0, 8), devId);
                    if (dev.hasInternet()) {
                        activeUploads.add(new UploadRecord(dev.getDeviceId(), p));
                        log.info("Packet {} reached bridge {}! Queued for upload.",
                                p.getPacketId().substring(0, 8), devId);
                    }
                }
            }
        });
    }

    private void share(String fromId, String toId, Map<String, Set<InboundPacketRequest>> toAdd) {
        VirtualDevice from = devices.get(fromId);
        VirtualDevice to = devices.get(toId);

        if (from == null || to == null) return;

        for (InboundPacketRequest p : from.getHeldPackets()) {
            // Only gossip if the target doesn't have it, and we have TTL remaining
            if (!to.holds(p.getPacketId()) && p.getTtl() > 0) {
                // Decrement TTL during transmission
                InboundPacketRequest shared = new InboundPacketRequest(
                        p.getPacketId(),
                        p.getTtl() - 1,
                        p.getCreatedAt(),
                        p.getCiphertext()
                );
                toAdd.get(toId).add(shared);
            }
        }
    }

    @Override
    public Collection<VirtualDevice> getDevices() {
        return devices.values();
    }

    @Override
    public VirtualDevice getDevice(String id) {
        return devices.get(id);
    }

    @Override
    public List<UploadRecord> getUploads() {
        return activeUploads;
    }

    @Override
    public void clearUploads() {
        activeUploads.clear();
    }

    @Override
    public void resetNetwork() {
        activeUploads.clear();
        devices.values().forEach(VirtualDevice::clear);
        log.info("Mesh network simulator state reset.");
    }
}
