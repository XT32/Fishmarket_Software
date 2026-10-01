package test;

import org.junit.jupiter.api.Test;
import utils.EnvConfig;

import static org.junit.jupiter.api.Assertions.*;

public class EnvConfigTest {

    @Test
    public void testGetWithDefault() {
        String nonExistent = EnvConfig.get("NON_EXISTENT_KEY_12345", "my-default-value");
        assertEquals("my-default-value", nonExistent);
    }

    @Test
    public void testGetInt() {
        int val = EnvConfig.getInt("NON_EXISTENT_PORT_9999", 5432);
        assertEquals(5432, val);
    }

    @Test
    public void testGetBoolean() {
        assertTrue(EnvConfig.getBoolean("NON_EXISTENT_BOOL_TRUE", true));
        assertFalse(EnvConfig.getBoolean("NON_EXISTENT_BOOL_FALSE", false));
    }

    @Test
    public void testEnvConfigLoadsExpectedKeys() {
        // DB_TYPE is defined in .env
        String dbType = EnvConfig.get("DB_TYPE");
        assertNotNull(dbType, "DB_TYPE should be resolved from .env or default");
        assertFalse(dbType.trim().isEmpty());
    }

    @Test
    public void testSystemPropertyOverride() {
        String testKey = "TEST_CUSTOM_PROPERTY_OVERRIDE";
        System.setProperty(testKey, "override_value");
        try {
            assertEquals("override_value", EnvConfig.get(testKey));
        } finally {
            System.clearProperty(testKey);
        }
    }
}
