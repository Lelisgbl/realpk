package view;

import infra.PokemonRepository;
import model.Move;
import model.Pokemon;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/** Terminal view for browsing and selecting Pokémon from the catalog. */
public class PokedexScreen {
    private final PokemonRepository pokemonRepository;

    public PokedexScreen(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    public void displayCatalog() {
        List<Pokemon> pokemon = pokemonRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Pokemon::getId))
                .toList();
        System.out.println("\n========== POKÉDEX ==========");
        if (pokemon.isEmpty()) {
            System.out.println("A Pokédex está vazia.");
            return;
        }
        for (Pokemon entry : pokemon) displayEntry(entry);
        System.out.println("=============================");
    }

    public Pokemon choosePokemon(Scanner input) {
        List<Pokemon> pokemon = pokemonRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Pokemon::getId))
                .toList();
        if (pokemon.isEmpty()) {
            System.out.println("Não há Pokémon disponíveis na Pokédex.");
            return null;
        }
        while (true) {
            System.out.println("\nEscolha seu Pokémon pelo número da Pokédex (0 para cancelar):");
            for (Pokemon entry : pokemon) {
                System.out.printf("%3d - %s (%s)%n", entry.getId(), entry.getName(), String.join("/", entry.getTypes()));
            }
            if (!input.hasNextLine()) return null;
            String answer = input.nextLine().trim();
            try {
                int id = Integer.parseInt(answer);
                if (id == 0) return null;
                Pokemon selected = pokemon.stream().filter(entry -> entry.getId() == id).findFirst().orElse(null);
                if (selected != null) {
                    System.out.println("Você escolheu " + selected.getName() + "!");
                    return selected;
                }
            } catch (NumberFormatException ignored) {
                // Continue prompting until a valid Pokédex ID is provided.
            }
            System.out.println("Número inválido. Tente novamente.");
        }
    }

    private void displayEntry(Pokemon pokemon) {
        System.out.printf("#%03d %-12s Tipo: %-14s Nv.%d | HP %d | ATQ %d | DEF %d | VEL %d%n",
                pokemon.getId(), pokemon.getName(), String.join("/", pokemon.getTypes()), pokemon.getLevel(),
                pokemon.getMaxHp(), pokemon.getAttack(), pokemon.getDefense(), pokemon.getSpeed());
        List<Move> moves = pokemon.getMoves();
        String movesText = moves.isEmpty() ? "sem golpes" : moves.stream()
                .map(move -> move.getName() + " (" + move.getType() + ")")
                .reduce((first, next) -> first + ", " + next).orElse("sem golpes");
        System.out.println("     Golpes: " + movesText);
        if (pokemon.getNextEvolutionId() > 0) {
            pokemonRepository.findById(pokemon.getNextEvolutionId())
                    .ifPresent(next -> System.out.println("     Próxima evolução: " + next.getName()));
        }
    }
}
