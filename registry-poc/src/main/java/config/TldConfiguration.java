package config;

import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Configuration for TLD (Top Level Domain) management.
 * 
 * Inspired by Nomulus's TLD configuration system, this provides:
 * - Multi-tenant TLD support
 * - Configurable pricing
 * - Environment-specific settings
 * - Premium domain configuration
 */
public class TldConfiguration {
    
    private final Map<String, TldConfig> tldConfigs;
    
    public TldConfiguration() {
        // Initialize with some sample TLD configurations
        this.tldConfigs = ImmutableMap.<String, TldConfig>builder()
            .put("com", new TldConfig("com", 10.00, true, Set.of("example", "test")))
            .put("org", new TldConfig("org", 12.00, true, Set.of()))
            .put("app", new TldConfig("app", 20.00, true, Set.of("cloud", "web", "api")))
            .put("dev", new TldConfig("dev", 25.00, true, Set.of("test", "demo", "sandbox")))
            .put("test", new TldConfig("test", 5.00, false, Set.of())) // Test TLD
            .build();
    }
    
    public boolean isSupportedTld(String tld) {
        return tldConfigs.containsKey(tld.toLowerCase());
    }
    
    public double getBasePriceForTld(String tld) {
        TldConfig config = tldConfigs.get(tld.toLowerCase());
        return config != null ? config.basePrice : 0.0;
    }
    
    public boolean isProductionTld(String tld) {
        TldConfig config = tldConfigs.get(tld.toLowerCase());
        return config != null && config.isProduction;
    }
    
    public Set<String> getPremiumKeywords(String tld) {
        TldConfig config = tldConfigs.get(tld.toLowerCase());
        return config != null ? config.premiumKeywords : Set.of();
    }
    
    public Set<String> getSupportedTlds() {
        return tldConfigs.keySet();
    }
    
    public TldConfig getTldConfig(String tld) {
        return tldConfigs.get(tld.toLowerCase());
    }
    
    /**
     * Configuration for a specific TLD
     */
    public static class TldConfig {
        private final String tld;
        private final double basePrice;
        private final boolean isProduction;
        private final Set<String> premiumKeywords;
        
        public TldConfig(String tld, double basePrice, boolean isProduction, Set<String> premiumKeywords) {
            this.tld = tld;
            this.basePrice = basePrice;
            this.isProduction = isProduction;
            this.premiumKeywords = ImmutableSet.copyOf(premiumKeywords);
        }
        
        public String getTld() {
            return tld;
        }
        
        public double getBasePrice() {
            return basePrice;
        }
        
        public boolean isProduction() {
            return isProduction;
        }
        
        public Set<String> getPremiumKeywords() {
            return premiumKeywords;
        }
        
        public boolean isPremiumKeyword(String keyword) {
            return premiumKeywords.contains(keyword.toLowerCase());
        }
    }
}