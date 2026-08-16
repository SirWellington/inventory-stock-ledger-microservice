package com.sirwellington.target.rest;

import java.util.Map;

import com.sirwellington.target.db.InventoryRepository;
import io.javalin.http.Context;

public class GetCurrentValueHandler {

    public record CurrentValueResponse(
        String skuId,
        int currentQuantity,
        java.math.BigDecimal totalCurrentValue
    ) {}

    private final InventoryRepository repository;

    public GetCurrentValueHandler(InventoryRepository repository) {
        this.repository = repository;
    }

    public void handle(Context ctx) throws Exception {
        String skuId = ctx.pathParam("skuId");

        var result = repository.getInventoryValue(new InventoryRepository.GetInventoryValueRequest(skuId));

        if (result.isEmpty()) {
            ctx.status(404).json(Map.of("error", "SKU not found: " + skuId));
            return;
        }

        var value = result.get();
        ctx.json(new CurrentValueResponse(skuId, value.currentQuantity(), value.totalCurrentValue()));
    }
}
