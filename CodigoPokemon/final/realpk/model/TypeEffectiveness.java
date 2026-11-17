package model;

import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Type chart for the 18 standard Pokémon types (Generation VI onward). */
public final class TypeEffectiveness {
    private record Row(Set<String> strong, Set<String> resisted, Set<String> immune) {}

    private static final Map<String, Row> CHART = new HashMap<>(Map.ofEntries(
        row("NORMAL", "", "ROCK STEEL", "GHOST"),
        row("FIRE", "GRASS ICE BUG STEEL", "FIRE WATER ROCK DRAGON", ""),
        row("WATER", "FIRE GROUND ROCK", "WATER GRASS DRAGON", ""),
        row("ELECTRIC", "WATER FLYING", "ELECTRIC GRASS DRAGON", "GROUND"),
        row("GRASS", "WATER GROUND ROCK", "FIRE GRASS POISON FLYING BUG DRAGON STEEL", ""),
        row("ICE", "GRASS GROUND FLYING DRAGON", "FIRE WATER ICE STEEL", ""),
        row("FIGHTING", "NORMAL ICE ROCK DARK STEEL", "POISON FLYING PSYCHIC BUG FAIRY", "GHOST"),
        row("POISON", "GRASS FAIRY", "POISON GROUND ROCK GHOST", "STEEL"),
        row("GROUND", "FIRE ELECTRIC POISON ROCK STEEL", "GRASS BUG", "FLYING"),
        row("FLYING", "GRASS FIGHTING BUG", "ELECTRIC ROCK STEEL", ""),
        row("PSYCHIC", "FIGHTING POISON", "PSYCHIC STEEL", "DARK"),
        row("BUG", "GRASS PSYCHIC DARK", "FIRE FIGHTING POISON FLYING GHOST STEEL FAIRY", ""),
        row("ROCK", "FIRE ICE FLYING BUG", "FIGHTING GROUND STEEL", ""),
        row("GHOST", "PSYCHIC GHOST", "DARK", "NORMAL"),
        row("DRAGON", "DRAGON", "STEEL", "FAIRY"),
        row("DARK", "PSYCHIC GHOST", "FIGHTING DARK FAIRY", ""),
        row("STEEL", "ICE ROCK FAIRY", "FIRE WATER ELECTRIC STEEL", ""),
        row("FAIRY", "FIGHTING DRAGON DARK", "FIRE POISON STEEL", "")
    ));

    private TypeEffectiveness() {}

    private static Map.Entry<String, Row> row(String type, String strong, String resisted, String immune) {
        return Map.entry(type, new Row(words(strong), words(resisted), words(immune)));
    }

    private static Set<String> words(String value) {
        return value.isBlank() ? Set.of() : Set.of(value.split(" "));
    }

    /** Registers or replaces an attack type row using space-separated defender types. */
    public static void registerType(String type, String strongAgainst, String resistedBy, String immuneAgainst) {
        String key = type.toUpperCase(Locale.ROOT);
        CHART.put(key, new Row(words(strongAgainst.toUpperCase(Locale.ROOT)),
                words(resistedBy.toUpperCase(Locale.ROOT)), words(immuneAgainst.toUpperCase(Locale.ROOT))));
    }

    /** Combines both defending types; an immunity in either type makes the result zero. */
    public static double multiplier(String attackType, Pokemon defender) {
        String attack = attackType == null ? "" : attackType.toUpperCase(Locale.ROOT);
        Row row = CHART.get(attack);
        if (row == null) return 1.0;
        double result = 1.0;
        for (String defenseType : defender.getTypes()) {
            String defense = defenseType.toUpperCase(Locale.ROOT);
            if (row.immune().contains(defense)) return 0.0;
            if (row.strong().contains(defense)) result *= 2.0;
            else if (row.resisted().contains(defense)) result *= 0.5;
        }
        return result;
    }

    public static String describe(double multiplier) {
        if (multiplier == 0.0) return "Não teve efeito.";
        if (multiplier > 1.0) return "Super efetivo!";
        if (multiplier < 1.0) return "Pouco efetivo.";
        return "Efeito normal.";
    }
}
