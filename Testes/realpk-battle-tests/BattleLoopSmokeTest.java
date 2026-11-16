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
        rival.takeDamage(rival.getCurrentHp() - 4);
        Pokemon randomRival = BattleEngine.chooseRandomOpponent(repo.findAll(), player, new Random(123));
        require(randomRival.getId() != player.getId(), "random rival matched player");
        require(!randomRival.getType().equals(player.getType()), "random rival did not prefer another type");
        boolean evolvedRivalCanAppear = false;
        Random rivalRandom = new Random(123);
        for (int i = 0; i < 100; i++) {
            Pokemon candidate = BattleEngine.chooseRandomOpponent(repo.findAll(), player, rivalRandom);
            if (candidate.getId() == 2 || candidate.getId() == 3 || candidate.getId() == 5
                    || candidate.getId() == 6 || candidate.getId() == 8 || candidate.getId() == 9
                    || candidate.getId() == 11 || candidate.getId() == 12 || candidate.getId() == 17
                    || candidate.getId() == 18 || candidate.getId() == 67 || candidate.getId() == 68) {
                evolvedRivalCanAppear = true;
            }
        }
        require(evolvedRivalCanAppear, "evolved rival forms never entered the random pool");
        BattleEngine engine = new BattleEngine(player, rival, repo, new Random(1));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        System.setOut(new PrintStream(captured));
        try {
            engine.startBattle(new Scanner("x\n1\n"), new BattleScreen(new AsciiArtRepository("ascii_arts.txt")));
        } finally { System.setOut(previous); }
        String log = captured.toString();
        require(engine.checkWinner() == player, "battle did not resolve to player victory");
        require(player.getExperience() > 0, "battle did not grant experience: " + player.getExperience());
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
        int damage = hp - water.getCurrentHp();
        require(damage > 0, "damage formula did not cause damage: " + damage);
        require(!water.isFainted(), "one attack should not knock out a full-health Pokemon");
        Pokemon strongWater = repo.findById(9).orElseThrow();
        strongWater.gainExperience(3500);
        Pokemon lowLevelFire = repo.findById(4).orElseThrow();
        require(strongWater.attack(0, lowLevelFire), "strong attack failed");
        require(lowLevelFire.isFainted(), "level advantage and super-effective move should be able to knock out");
        Pokemon groundAttacker = repo.findById(1).orElseThrow();
        groundAttacker.addMove(new Move("Ground Test", 50, 100, "GROUND", 5));
        int charizardHp = charizard.getCurrentHp();
        groundAttacker.attack(1, charizard);
        require(charizard.getCurrentHp() == charizardHp, "immune target lost HP");
        require(hp > 0, "test setup invalid");
        System.out.println("BATTLE_AND_TYPE_TESTS_OK");
    }
}
