package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.ProductClient;
import by.shakhau.ps.order.client.dto.Product;
import by.shakhau.ps.order.client.dto.ProductIndices;
import by.shakhau.ps.order.exception.ResourceNotFoundException;
import by.shakhau.ps.order.repository.ProductSnapshotRepository;
import by.shakhau.ps.order.repository.entity.ProductSnapshotEntity;
import by.shakhau.ps.order.service.ProductSnapshotService;
import by.shakhau.ps.order.service.mapper.ProductSnapshotMapper;
import by.shakhau.ps.order.service.model.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSnapshotServiceImpl implements ProductSnapshotService {

    private final ProductClient client;
    private final ProductSnapshotMapper mapper;
    private final ProductSnapshotRepository repository;

    @Override
    public Map<UUID, ProductSnapshot> fetchByProductIds(Collection<UUID> productIds) {
        List<Product> products = client.findProducts(new ProductIndices(productIds));
        return products.stream()
                .map(p -> repository.findByProductIdAndPrice(p.getId(), p.getPrice())
                        .orElseGet(() -> {
                            var productSnapshot = ProductSnapshotEntity.builder()
                                    .productId(p.getId())
                                    .name(p.getName())
                                    .price(p.getPrice())
                                    .build();
                            try {
                                return repository.save(productSnapshot);
                            } catch (DataIntegrityViolationException e) {
                                return repository.findByProductIdAndPrice(p.getId(), p.getPrice())
                                        .orElseThrow(() -> new ResourceNotFoundException("Product snapshot not found."));
                            }
                        }))
                .collect(Collectors.toMap(ProductSnapshotEntity::getProductId, mapper::toModel));
    }
}
