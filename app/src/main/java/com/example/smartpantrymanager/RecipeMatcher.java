package com.example.smartpantrymanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {
    private final HashMap<String, String> ingredientAliases = new HashMap<>();

    public RecipeMatcher(Map<String, String> aliases) {
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            ingredientAliases.put(normalizeName(alias.getKey()), alias.getValue());
        }
    }

    public static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Z}]+", " ").trim();
    }

    public ArrayList<RecipeSuggestion> findMatches(List<PantryItem> pantry,
                                                  List<RecipeVariant> variants) {
        HashMap<String, BigDecimal> available = totalPantry(pantry);
        ArrayList<RecipeSuggestion> suggestions = new ArrayList<>();
        HashMap<String, RecipeSuggestion> recipesById = new HashMap<>();

        for (RecipeVariant variant : variants) {
            if (hasAllRequirements(available, variant.getRequirements())) {
                RecipeSuggestion suggestion = recipesById.get(variant.getRecipeId());
                if (suggestion == null) {
                    suggestion = new RecipeSuggestion(variant.getRecipeId(), variant.getTitle());
                    recipesById.put(variant.getRecipeId(), suggestion);
                    suggestions.add(suggestion);
                }
                suggestion.addVersion(variant);
            }
        }
        return suggestions;
    }

    private HashMap<String, BigDecimal> totalPantry(List<PantryItem> pantry) {
        HashMap<String, BigDecimal> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String ingredientId = ingredientAliases.get(normalizeName(item.getName()));
            if (ingredientId != null && isValidQuantity(item.getQuantity())
                    && baseUnit(item.getUnit()) != null) {
                addQuantity(totals, ingredientId, item.getQuantity(), item.getUnit());
            }
        }
        return totals;
    }

    private boolean hasAllRequirements(HashMap<String, BigDecimal> available,
                                       ArrayList<RecipeRequirement> requirements) {
        if (requirements.isEmpty()) {
            return false;
        }
        HashMap<String, BigDecimal> requiredTotals = new HashMap<>();
        for (RecipeRequirement requirement : requirements) {
            if ("water".equals(requirement.getIngredientId()) || "salt".equals(requirement.getIngredientId())) {
                continue;
            }
            if (!isValidQuantity(requirement.getQuantity()) || baseUnit(requirement.getUnit()) == null) {
                return false;
            }
            addQuantity(requiredTotals, requirement.getIngredientId(),
                    requirement.getQuantity(), requirement.getUnit());
        }
        for (Map.Entry<String, BigDecimal> requirement : requiredTotals.entrySet()) {
            BigDecimal quantityAvailable = available.get(requirement.getKey());
            if (quantityAvailable == null || quantityAvailable.compareTo(requirement.getValue()) < 0) {
                return false;
            }
        }
        return true;
    }

    private void addQuantity(HashMap<String, BigDecimal> totals, String ingredientId,
                             double quantity, String unit) {
        // The unit is part of the key: 500 ml can never satisfy a requirement for 500 g.
        String key = ingredientId + ":" + baseUnit(unit);
        BigDecimal convertedQuantity = BigDecimal.valueOf(quantity);
        if ("kg".equals(unit) || "l".equals(unit)) {
            convertedQuantity = convertedQuantity.multiply(BigDecimal.valueOf(1000));
        }
        BigDecimal previousQuantity = totals.get(key);
        if (previousQuantity == null) {
            previousQuantity = BigDecimal.ZERO;
        }
        // Decimal arithmetic avoids a rounding tolerance that could allow a shortfall.
        totals.put(key, previousQuantity.add(convertedQuantity));
    }

    private String baseUnit(String unit) {
        if ("g".equals(unit) || "kg".equals(unit)) {
            return "g";
        } else if ("ml".equals(unit) || "l".equals(unit)) {
            return "ml";
        } else if ("count".equals(unit)) {
            return "count";
        }
        return null;
    }

    private boolean isValidQuantity(double quantity) {
        return quantity > 0 && !Double.isNaN(quantity) && !Double.isInfinite(quantity);
    }
}
