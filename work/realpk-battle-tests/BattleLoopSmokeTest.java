import infra.AsciiArtRepository;
import infra.JsonPokemonRepository;
import model.BattleEngine;
import model.Move;
import model.Pokemon;
import model.TypeEffectiveness;
import view.BattleScreen;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;

public class BattleLoopSmokeTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        JsonPokemonRepository repo = new JsonPokemonRepository("pokemons.json");
        Pokemon player = repo.findById(4).orElseThrow();
        Pokemon rival = new JsonPokemonRepository("pokemons.json").findById(1).orElseThrow();
        rival.takeDamage(rival.getCurrentHp() - 1);
        Pokemon randomRival = BattleEngine.chooseRandomOpponent(repo.findAll(), player, new Random(123));
        require(randomRival.getId() != player.getId(), "random rival matched player");
        require(!randomRival.getType().equals(player.getType()), "random rival did not prefer another type");
        BattleEngine engine = new BattleEngine(player, rival, repo, new Random(1));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        System.setOut(new PrintStream(captured));
        try {
            engine.startBattle(new Scanner("x\n1\n"), new BattleScreen(new AsciiArtRepository("ascii_arts.txt")));
        } finally { System.setOut(previous); }
        String log = captured.toString();
        require(engine.checkWinner() == player, "battle did not resolve to player victory");
        require(player.getExperience() == 525, "4x victory reward plus attack XP not granted: " + player.getExperience());
        require(log.contains("Escolha inválida"), "invalid choice was not handled");
        require(log.contains("Vencedor:"), "winner was not announced");

        Pokemon water = repo.findById(7).orElseThrow();
        Pokemon grass = repo.findById(2).orElseThrow();
        Pokemon fire = repo.findById(4).orElseThrow();
        Pokemon charizard = repo.findById(6).orElseThrow();
        require(TypeEffectiveness.multiplier("GRASS", water) == 2.0, "grass should be super effective vs water");
        require(TypeEffectiveness.multiplier("FIRE", grass) == 2.0, "fire should be super effective vs grass");
        require(TypeEffectiveness.multiplier("WATER", fire) == 2.0, "water should be super effective vs fire");
        require(TypeEffectiveness.multiplier("GROUND", charizard) == 0.0, "ground should not affect Charizard");
        int hp = water.getCurrentHp();
        require(grass.attack(0, water), "Razor Leaf attack failed");
        require(water.isFainted(), "super-effective Razor Leaf should defeat this HP sample");
        Pokemon groundAttacker = repo.findById(1).orElseThrow();
        groundAttacker.addMove(new Move("Ground Test", 50, 100, "GROUND", 5));
        int charizardHp = charizard.getCurrentHp();
        groundAttacker.attack(1, charizard);
        require(charizard.getCurrentHp() == charizardHp, "immune target lost HP");
        require(hp > 0, "test setup invalid");
        System.out.println("BATTLE_AND_TYPE_TESTS_OK");
    }
}
