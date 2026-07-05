package com.demo.offline_upi.aspect;

import com.demo.offline_upi.dto.InboundPacketRequest;
import com.demo.offline_upi.dto.TransactionStatusResponse;
import com.demo.offline_upi.exception.DecryptionFailedException;
import com.demo.offline_upi.exception.DuplicatePacketException;
import com.demo.offline_upi.exception.TransactionReplayedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect logging structural metadata, execution latency, and outcomes
 * of incoming packet ingestion pipelines.
 */
@Aspect
@Component
public class IngestionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(IngestionLoggingAspect.class);

    @Pointcut("execution(* com.demo.offline_upi.service.BridgeIngestionService.ingest(..))")
    public void ingestPointcut() {}

    @Around("ingestPointcut()")
    public Object auditIngest(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        InboundPacketRequest request = (InboundPacketRequest) args[0];
        String bridgeNodeId = (String) args[1];
        Integer hopCount = (Integer) args[2];

        long startTime = System.currentTimeMillis();
        String outcome = "INVALID";
        String exceptionName = null;

        try {
            Object result = joinPoint.proceed();
            if (result instanceof TransactionStatusResponse response) {
                outcome = response.getOutcome();
            }
            return result;
        } catch (DuplicatePacketException e) {
            outcome = "DUPLICATE";
            exceptionName = e.getClass().getSimpleName();
            throw e;
        } catch (DecryptionFailedException | TransactionReplayedException | IllegalArgumentException e) {
            outcome = "INVALID";
            exceptionName = e.getClass().getSimpleName();
            throw e;
        } catch (Throwable e) {
            outcome = "ERROR";
            exceptionName = e.getClass().getSimpleName();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AUDIT] Ingestion on Bridge Node: {} | Packet ID: {} | Hops: {} | Execution Time: {}ms | Outcome: {}{}",
                    bridgeNodeId,
                    request.getPacketId().substring(0, 8),
                    hopCount,
                    duration,
                    outcome,
                    exceptionName != null ? " (" + exceptionName + ")" : ""
            );
        }
    }
}
