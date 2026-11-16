import infra.JsonPokemonRepository;
import infra.SaveGameManager;
import model.Move;
import model.Pokemon;

import java.nio.file.Files;

public class SaveGameManagerTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        String savePath = "work/realpk-battle-tests/test-save.properties";
        Files.deleteIfExists(java.nio.file.Path.of(savePath));
        JsonPokemonRepository repository = new JsonPokemonRepository("CodigoDescompactado/parcial/realpk/pokemons.json");
        SaveGameManager manager = new SaveGameManager(savePath, repository);
        Pokemon charmander = repository.findById(4).orElseThrow();
        charmander.gainExperience(500);
        charmander.evolveTo(repository.findById(5).orElseThrow());
        charmander.getMoves().get(0).useMove();

        manager.save(charmander);
        Pokemon loaded = manager.load().orElseThrow();
        check(loaded.getId() == 5 && loaded.getName().equals("Charmeleon"), "evolved form not restored");
        check(loaded.getLevel() == charmander.getLevel(), "level not restored");
        check(loaded.getExperience() == charmander.getExperience(), "experience not restored");
        check(loaded.getAttack() == charmander.getAttack() && loaded.getMaxHp() == charmander.getMaxHp(), "stats not restored");
        check(loaded.getMoves().size() == charmander.getMoves().size(), "moves not restored");
        check(loaded.getMoves().get(0).getCurrentPP() == charmander.getMoves().get(0).getCurrentPP(), "move PP not restored");
        loaded.takeDamage(loaded.getCurrentHp());
        loaded.getMoves().get(0).useMove();
        loaded.prepareForBattle();
        check(!loaded.isFainted() && loaded.getCurrentHp() == loaded.getMaxHp(), "fainted saved Pokémon was not ready for the next match");
        check(loaded.getMoves().get(0).getCurrentPP() == loaded.getMoves().get(0).getMaxPP(), "depleted PP was not restored for the next match");
        manager.reset();
        check(!manager.hasSave() && manager.load().isEmpty(), "reset did not clear save");
        System.out.println("SAVE_LOAD_RESET_TESTS_OK");
    }
}
