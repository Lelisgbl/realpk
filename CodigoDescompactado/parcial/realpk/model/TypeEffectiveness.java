package model;

import java.util.Locale;

public final class TypeEffectiveness {
    private TypeEffectiveness() {}

    public static double multiplier(String attackType, Pokemon defender) {
        String attack = attackType == null ? "" : attackType.toUpperCase(Locale.ROOT);
        String defense = defender.getType().toUpperCase(Locale.ROOT);

        if ("GROUND".equals(attack) && "CHARIZARD".equalsIgnoreCase(defender.getName())) return 0.0;

        return switch (attack) {
            case "FIRE" -> switch (defense) {
                case "GRASS" -> 2.0;
                case "WATER" -> 0.5;
                default -> 1.0;
            };
            case "WATER" -> switch (defense) {
                case "FIRE" -> 2.0;
                case "GRASS" -> 0.5;
                default -> 1.0;
            };
            case "GRASS" -> switch (defense) {
                case "WATER" -> 2.0;
                case "FIRE" -> 0.5;
                default -> 1.0;
            };
            case "ELECTRIC" -> "WATER".equals(defense) ? 2.0 : 1.0;
            case "GROUND" -> "FIRE".equals(defense) ? 2.0 : 1.0;
            default -> 1.0;
        };
    }

    public static String describe(double multiplier) {
        if (multiplier == 0.0) return "Não teve efeito.";
        if (multiplier > 1.0) return "Super efetivo!";
        if (multiplier < 1.0) return "Pouco efetivo.";
        return "Efeito normal.";
    }
}