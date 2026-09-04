package by.shakhau.ps.order.service;

import by.shakhau.ps.order.service.model.ProductSnapshot;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ProductSnapshotService {

    Map<UUID, ProductSnapshot> fetchByProductIds(Collection<UUID> productIds);
}
