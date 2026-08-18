package com.sirwellington.target.rest;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.javalin.http.Context;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@AlchemyTest
class GetHealthHandlerTest {

    @Test
    void testReturnsOkStatus() throws Exception {
        Context ctx = mock(Context.class);

        var handler = new GetHealthHandler();
        handler.handle(ctx);

        verify(ctx).json(Map.of("status", "ok"));
    }
}
