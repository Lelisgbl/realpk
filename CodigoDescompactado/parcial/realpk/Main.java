import infra.AsciiArtRepository;
import infra.JsonPokemonRepository;
import infra.PokemonRepository;
import model.BattleEngine;
import model.Pokemon;
import view.BattleScreen;
import view.PokedexScreen;

import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PokemonRepository pokemonRepository = new JsonPokemonRepository("pokemons.json");
        PokedexScreen pokedex = new PokedexScreen(pokemonRepository);
        BattleScreen battleScreen = new BattleScreen(new AsciiArtRepository("ascii_arts.txt"));

        try (Scanner input = new Scanner(System.in)) {
            runMainMenu(input, pokemonRepository, pokedex, battleScreen);
        }
    }

    private static void runMainMenu(Scanner input, PokemonRepository repository,
                                    PokedexScreen pokedex, BattleScreen battleScreen) {
        while (true) {
            System.out.println("\n===== POKÉMON =====");
            System.out.println("[1] Consultar Pokédex");
            System.out.println("[2] Iniciar batalha");
            System.out.println("[0] Sair");
            System.out.print("Escolha uma opção: ");
            if (!input.hasNextLine()) return;
            switch (input.nextLine().trim()) {
                case "1" -> pokedex.displayCatalog();
                case "2" -> startBattle(input, repository, pokedex, battleScreen);
                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void startBattle(Scanner input, PokemonRepository repository,
                                    PokedexScreen pokedex, BattleScreen battleScreen) {
        Pokemon player = pokedex.choosePokemon(input);
        if (player == null) {
            System.out.println("Seleção cancelada.");
            return;
        }
        Pokemon opponent = BattleEngine.chooseRandomOpponent(repository.findAll(), player, new Random());
        battleScreen.displayMessage(player.getName() + " enfrentará " + opponent.getName() + "!");
        new BattleEngine(player, opponent, repository).startBattle(input, battleScreen);
    }
}
