package infra;

import model.Pokemon;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface PokemonRepository {
    List<Pokemon> findAll();
    default List<Pokemon> findBaseSpecies() {
        List<Pokemon> catalog = findAll();
        Set<Integer> evolvedIds = catalog.stream().map(Pokemon::getNextEvolutionId)
                .filter(id -> id > 0).collect(Collectors.toSet());
        return catalog.stream().filter(pokemon -> !evolvedIds.contains(pokemon.getId())).toList();
    }
    Optional<Pokemon> findByName(String name);
    Optional<Pokemon> findById(int id);
}
