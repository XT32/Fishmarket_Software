package test;

import model.Ikan;
import org.junit.jupiter.api.Test;
import service.RedisCacheService;
import utils.RedisManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RedisCacheServiceTest {

    @Test
    public void testCacheAndInvalidateCycle() {
        if (!RedisManager.isAvailable()) {
            System.out.println("Redis server is not running or disabled; skipping live Redis test.");
            // Verify graceful behavior when unavailable
            assertNull(RedisCacheService.getCachedFishCatalog());
            assertDoesNotThrow(RedisCacheService::invalidateCatalogCache);
            return;
        }

        // Test with live Redis
        List<Ikan> mockList = new ArrayList<>();
        mockList.add(new Ikan(9991, "Ikan Tuna Mock", 75000.0, "/img/mock_tuna.png", 50, 1));
        mockList.add(new Ikan(9992, "Ikan Salmon Mock", 120000.0, "/img/mock_salmon.png", 30, 1));

        RedisCacheService.cacheFishCatalog(mockList);

        List<Ikan> cached = RedisCacheService.getCachedFishCatalog();
        assertNotNull(cached, "Cached fish list should not be null when Redis is active");
        assertEquals(2, cached.size(), "Cached fish list size should match");
        assertEquals("Ikan Tuna Mock", cached.get(0).getNamaIkan());
        assertEquals(75000.0, cached.get(0).getHarga());

        // Invalidate and verify
        RedisCacheService.invalidateCatalogCache();
        List<Ikan> afterInvalidation = RedisCacheService.getCachedFishCatalog();
        assertNull(afterInvalidation, "Cached catalog should be null after invalidation");
    }
}
