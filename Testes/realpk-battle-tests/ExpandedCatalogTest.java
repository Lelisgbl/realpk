import infra.JsonPokemonRepository;
import model.Pokemon;
import model.GenericPokemon;
import model.TypeEffectiveness;

import java.util.List;

public class ExpandedCatalogTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static List<String> moveNames(Pokemon pokemon) {
        return pokemon.getMoves().stream().map(m -> m.getName()).toList();
    }

    public static void main(String[] args) {
        JsonPokemonRepository repository = new JsonPokemonRepository("CodigoDescompactado/parcial/realpk/pokemons.json");
        List<Pokemon> all = repository.findAll();
        check(all.size() == 18, "catalog must contain 18 forms across six lines");
        for (int id : new int[]{1, 4, 7, 16, 10, 66}) {
            Pokemon base = repository.findById(id).orElseThrow();
            check(base.getNextEvolutionId() > 0, "base species missing evolution id: " + base.getName());
        }

        Pokemon charmander = repository.findById(4).orElseThrow();
        check(moveNames(charmander).equals(List.of("Quick Attack")), "Charmander should start with Quick Attack only");
        int initialHp = charmander.getMaxHp();
        int initialAttack = charmander.getAttack();
        Pokemon charmeleon = repository.findById(charmander.getNextEvolutionId()).orElseThrow();
        charmander.evolveTo(charmeleon);
        check(moveNames(charmander).equals(List.of("Quick Attack", "Fire Fang")), "Charmeleon move inheritance incorrect");
        check(charmander.getMaxHp() > initialHp && charmander.getAttack() > initialAttack, "evolution did not raise stats");
        Pokemon charizard = repository.findById(charmander.getNextEvolutionId()).orElseThrow();
        charmander.evolveTo(charizard);
        check(moveNames(charmander).equals(List.of("Quick Attack", "Fire Fang", "Fire Blast")), "Charizard move inheritance incorrect");
        check(charmander.getTypes().equals(List.of("FIRE", "FLYING")), "Charizard dual typing missing");

        Pokemon caterpieLine = repository.findById(10).orElseThrow();
        caterpieLine.evolveTo(repository.findById(caterpieLine.getNextEvolutionId()).orElseThrow());
        check(moveNames(caterpieLine).equals(List.of("Bug Bite")), "Metapod should carry its existing attack");
        caterpieLine.evolveTo(repository.findById(caterpieLine.getNextEvolutionId()).orElseThrow());
        check(moveNames(caterpieLine).equals(List.of("Bug Bite", "Bug Buzz", "Air Slash")), "Butterfree moves incorrect");

        Pokemon pidgey = repository.findById(16).orElseThrow();
        Pokemon water = repository.findById(7).orElseThrow();
        Pokemon grass = repository.findById(1).orElseThrow();
        Pokemon machop = repository.findById(66).orElseThrow();
        Pokemon caterpie = repository.findById(10).orElseThrow();
        check(TypeEffectiveness.multiplier("GROUND", pidgey) == 0, "Flying immunity to Ground missing");
        check(TypeEffectiveness.multiplier("ELECTRIC", pidgey) == 2, "Electric vs Flying mismatch");
        check(TypeEffectiveness.multiplier("GRASS", water) == 2, "Grass vs Water mismatch");
        check(TypeEffectiveness.multiplier("FIRE", grass) == 2, "Fire vs Grass mismatch");
        check(TypeEffectiveness.multiplier("WATER", repository.findById(4).orElseThrow()) == 2, "Water vs Fire mismatch");
        check(TypeEffectiveness.multiplier("PSYCHIC", machop) == 2, "Psychic vs Fighting mismatch");
        check(TypeEffectiveness.multiplier("FIRE", caterpie) == 2, "Fire vs Bug mismatch");
        check(TypeEffectiveness.multiplier("NORMAL", repository.findById(1).orElseThrow()) == 1, "neutral matchup mismatch");
        check(TypeEffectiveness.multiplier("GROUND", charmander) == 0, "dual type immunity was not applied");
        check(TypeEffectiveness.multiplier("GRASS", pidgey) == 0.5, "Flying resistance mismatch");
        Pokemon waterFlying = new GenericPokemon("Water Flying test", "WATER", 5, 20, 10, 10, 10);
        waterFlying.setSecondaryType("FLYING");
        check(TypeEffectiveness.multiplier("GRASS", waterFlying) == 1, "dual-type multipliers were not combined");

        Pokemon clone = repository.findById(6).orElseThrow().copy();
        check(clone.getTypes().equals(List.of("FIRE", "FLYING")), "copy did not preserve secondary type");
        System.out.println("EXPANDED_CATALOG_AND_TYPE_TESTS_OK");
    }
}
