package service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Ikan;
import utils.RedisManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to manage Redis caching for fish catalogs, stock counters, and sessions.
 */
public class RedisCacheService {
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);
    private static final Gson gson = new Gson();

    private static final String KEY_FISH_CATALOG = "fishmarket:catalog:all";
    private static final String KEY_TOTAL_STOCK = "fishmarket:stats:total_stock";

    // Simple DTO for clean JSON serialization of Ikan without JavaFX Property wrappers
    public static class IkanDTO {
        public int idIkan;
        public String namaIkan;
        public double harga;
        public String gambarIkan;
        public int stok;
        public int idNelayan;

        public IkanDTO() {}

        public IkanDTO(Ikan ikan) {
            this.idIkan = ikan.getIdIkan();
            this.namaIkan = ikan.getNamaIkan();
            this.harga = ikan.getHarga();
            this.gambarIkan = ikan.getGambarIkan();
            this.stok = ikan.getStok();
            this.idNelayan = ikan.getIdNelayan();
        }

        public Ikan toIkan() {
            return new Ikan(idIkan, namaIkan, harga, gambarIkan, stok, idNelayan);
        }
    }

    public static List<Ikan> getCachedFishCatalog() {
        if (!RedisManager.isAvailable()) return null;

        try {
            String json = RedisManager.get(KEY_FISH_CATALOG);
            if (json != null && !json.trim().isEmpty()) {
                Type listType = new TypeToken<List<IkanDTO>>() {}.getType();
                List<IkanDTO> dtoList = gson.fromJson(json, listType);
                if (dtoList != null) {
                    List<Ikan> result = new ArrayList<>();
                    for (IkanDTO dto : dtoList) {
                        result.add(dto.toIkan());
                    }
                    logger.debug("Loaded {} fish items from Redis cache", result.size());
                    return result;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse cached fish catalog from Redis: {}", e.getMessage());
        }
        return null;
    }

    public static void cacheFishCatalog(List<Ikan> list) {
        if (!RedisManager.isAvailable() || list == null) return;

        try {
            List<IkanDTO> dtoList = new ArrayList<>();
            for (Ikan ikan : list) {
                dtoList.add(new IkanDTO(ikan));
            }
            String json = gson.toJson(dtoList);
            RedisManager.set(KEY_FISH_CATALOG, json);
            logger.debug("Saved {} fish items to Redis cache", list.size());
        } catch (Exception e) {
            logger.warn("Failed to save fish catalog to Redis: {}", e.getMessage());
        }
    }

    public static void invalidateCatalogCache() {
        if (!RedisManager.isAvailable()) return;
        RedisManager.del(KEY_FISH_CATALOG);
        RedisManager.del(KEY_TOTAL_STOCK);
        logger.debug("Redis fish catalog cache invalidated");
    }
}
