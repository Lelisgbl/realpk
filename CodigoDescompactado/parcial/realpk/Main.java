import infra.AsciiArtRepository;
import infra.JsonPokemonRepository;
import infra.PokemonRepository;
import model.BattleEngine;
import model.Pokemon;
import view.BattleScreen;

import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PokemonRepository pokemonRepo = new JsonPokemonRepository("pokemons.json");
        AsciiArtRepository artRepo = new AsciiArtRepository("ascii_arts.txt");
        BattleScreen battleScreen = new BattleScreen(artRepo);

        Pokemon player = pokemonRepo.findByName("CHARMANDER")
                .orElseThrow(() -> new IllegalStateException("Charmander não encontrado no catálogo."));
        Pokemon opponent = BattleEngine.chooseRandomOpponent(pokemonRepo.findAll(), player, new Random());

        battleScreen.displayMessage(player.getName() + " enfrentará " + opponent.getName() + "!");
        BattleEngine battleEngine = new BattleEngine(player, opponent, pokemonRepo);
        battleEngine.startBattle(new Scanner(System.in), battleScreen);
    }
}