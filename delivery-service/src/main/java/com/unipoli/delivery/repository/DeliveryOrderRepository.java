package com.unipoli.delivery.repository;

import com.unipoli.delivery.model.DeliveryOrder;
import com.unipoli.delivery.model.DeliveryPlatform;
import com.unipoli.delivery.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {

    Optional<DeliveryOrder> findByPlatformAndExternalOrderId(DeliveryPlatform platform, String externalOrderId);

    boolean existsByPlatformAndExternalOrderId(DeliveryPlatform platform, String externalOrderId);

    List<DeliveryOrder> findByPlatform(DeliveryPlatform platform);

    List<DeliveryOrder> findByStatus(DeliveryStatus status);

    Optional<DeliveryOrder> findByInternalOrderId(Long internalOrderId);

    List<DeliveryOrder> findAllByOrderByCreatedAtDesc();
}
