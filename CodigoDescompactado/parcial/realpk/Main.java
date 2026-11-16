import infra.AsciiArtRepository;
import infra.JsonPokemonRepository;
import infra.PokemonRepository;
import infra.SaveGameManager;
import model.BattleEngine;
import model.Pokemon;
import view.BattleScreen;
import view.PokedexScreen;

import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PokemonRepository pokemonRepository = new JsonPokemonRepository("pokemons.json");
        SaveGameManager saveGame = new SaveGameManager("pokemon-save.properties", pokemonRepository);
        PokedexScreen pokedex = new PokedexScreen(pokemonRepository);
        BattleScreen battleScreen = new BattleScreen(new AsciiArtRepository("ascii_arts.txt"));

        try (Scanner input = new Scanner(System.in)) {
            runMainMenu(input, pokemonRepository, pokedex, battleScreen, saveGame);
        }
    }

    private static void runMainMenu(Scanner input, PokemonRepository repository,
                                    PokedexScreen pokedex, BattleScreen battleScreen, SaveGameManager saveGame) {
        while (true) {
            System.out.println("\n===== POKÉMON =====");
            System.out.println("[1] Consultar Pokédex");
            System.out.println(saveGame.hasSave() ? "[2] Continuar jogo salvo" : "[2] Novo jogo");
            System.out.println("[3] Resetar progresso salvo");
            System.out.println("[0] Sair");
            System.out.print("Escolha uma opção: ");
            if (!input.hasNextLine()) return;
            try {
                switch (input.nextLine().trim()) {
                    case "1" -> pokedex.displayCatalog();
                    case "2" -> startBattle(input, repository, pokedex, battleScreen, saveGame);
                    case "3" -> resetProgress(input, saveGame);
                    case "0" -> { return; }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RuntimeException e) {
                System.out.println("Não foi possível concluir a operação: " + e.getMessage());
            }
        }
    }

    private static void startBattle(Scanner input, PokemonRepository repository,
                                    PokedexScreen pokedex, BattleScreen battleScreen, SaveGameManager saveGame) {
        Pokemon player = saveGame.load().orElseGet(() -> pokedex.choosePokemon(input));
        if (player == null) {
            System.out.println("Seleção cancelada.");
            return;
        }
        player.prepareForBattle();
        Pokemon opponent = BattleEngine.chooseRandomOpponent(repository.findAll(), player, new Random());
        battleScreen.displayMessage(player.getName() + " enfrentará " + opponent.getName() + "!");
        new BattleEngine(player, opponent, repository).startBattle(input, battleScreen);
        saveGame.save(player);
        System.out.println("Progresso salvo.");
    }

    private static void resetProgress(Scanner input, SaveGameManager saveGame) {
        System.out.print("Isso apagará o Pokémon salvo e todo o progresso. Confirma? (s/N): ");
        if (input.hasNextLine() && input.nextLine().trim().equalsIgnoreCase("s")) {
            saveGame.reset();
            System.out.println("Progresso resetado. Você poderá escolher um novo Pokémon.");
        } else {
            System.out.println("Reset cancelado.");
        }
    }
}
