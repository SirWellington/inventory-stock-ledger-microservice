package com.sirwellington.target.rest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.sirwellington.target.db.InventoryRepository;

import io.javalin.http.Context;
import tech.sirwellington.alchemy.test.AlchemyTest;
import tech.sirwellington.alchemy.test.generation.GenerateString;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@AlchemyTest
class GetCurrentValueHandlerTest {

    @Mock InventoryRepository repository;
    @GenerateString String skuId;

    private GetCurrentValueHandler createHandler() {
        return new GetCurrentValueHandler(repository);
    }

    @Test
    void testReturnsCurrentValueWhenSkuExists() throws Exception {
        var expectedValue = new InventoryRepository.GetInventoryValueResponse(250, new BigDecimal("12500.00"));
        when(repository.getInventoryValue(any())).thenReturn(Optional.of(expectedValue));

        Context ctx = mock(Context.class);
        when(ctx.pathParam("skuId")).thenReturn(skuId);

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(new GetCurrentValueHandler.CurrentValueResponse(skuId, 250, new BigDecimal("12500.00")));
    }

    @Test
    void testReturnsZeroQuantityWhenInStockButEmpty() throws Exception {
        var zeroValue = new InventoryRepository.GetInventoryValueResponse(0, BigDecimal.ZERO);
        when(repository.getInventoryValue(any())).thenReturn(Optional.of(zeroValue));

        Context ctx = mock(Context.class);
        when(ctx.pathParam("skuId")).thenReturn(skuId);

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(new GetCurrentValueHandler.CurrentValueResponse(skuId, 0, BigDecimal.ZERO));
    }

    @Test
    void testReturns404WhenSkuNotFound() throws Exception {
        when(repository.getInventoryValue(any())).thenReturn(Optional.empty());

        Context ctx = mock(Context.class);
        when(ctx.pathParam("skuId")).thenReturn(skuId);
        lenient().when(ctx.status(org.mockito.ArgumentMatchers.anyInt())).thenReturn(ctx);

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).status(404);
    }

    @Test
    void testReturnsErrorDetailOnNotFound() throws Exception {
        when(repository.getInventoryValue(any())).thenReturn(Optional.empty());

        Context ctx = mock(Context.class);
        when(ctx.pathParam("skuId")).thenReturn(skuId);
        lenient().when(ctx.status(org.mockito.ArgumentMatchers.anyInt())).thenReturn(ctx);

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(Map.of("error", "SKU not found: " + skuId));
    }

    @Test
    void testHandlesLargeInventoryValues() throws Exception {
        var largeValue = new InventoryRepository.GetInventoryValueResponse(10000, new BigDecimal("999999.99"));
        when(repository.getInventoryValue(any())).thenReturn(Optional.of(largeValue));

        Context ctx = mock(Context.class);
        when(ctx.pathParam("skuId")).thenReturn(skuId);

        var handler = createHandler();
        handler.handle(ctx);

        verify(ctx).json(new GetCurrentValueHandler.CurrentValueResponse(skuId, 10000, new BigDecimal("999999.99")));
    }
}
