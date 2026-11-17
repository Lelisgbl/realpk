package infra;

import model.Move;
import model.Pokemon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/** Loads, writes, and resets the single-player progress file. */
public final class SaveGameManager {
    private final Path savePath;
    private final PokemonRepository pokemonRepository;

    public SaveGameManager(String saveFile, PokemonRepository pokemonRepository) {
        this.savePath = Path.of(saveFile);
        this.pokemonRepository = pokemonRepository;
    }

    public boolean hasSave() {
        return Files.isRegularFile(savePath);
    }

    public void save(Pokemon pokemon) {
        Properties data = new Properties();
        data.setProperty("pokemon.id", Integer.toString(pokemon.getId()));
        data.setProperty("pokemon.level", Integer.toString(pokemon.getLevel()));
        data.setProperty("pokemon.experience", Integer.toString(pokemon.getExperience()));
        data.setProperty("pokemon.currentHp", Integer.toString(pokemon.getCurrentHp()));
        data.setProperty("pokemon.maxHp", Integer.toString(pokemon.getMaxHp()));
        data.setProperty("pokemon.attack", Integer.toString(pokemon.getAttack()));
        data.setProperty("pokemon.defense", Integer.toString(pokemon.getDefense()));
        data.setProperty("pokemon.speed", Integer.toString(pokemon.getSpeed()));
        List<Move> moves = pokemon.getMoves();
        data.setProperty("moves.count", Integer.toString(moves.size()));
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            String prefix = "moves." + i + ".";
            data.setProperty(prefix + "name", move.getName());
            data.setProperty(prefix + "power", Integer.toString(move.getPower()));
            data.setProperty(prefix + "accuracy", Integer.toString(move.getAccuracy()));
            data.setProperty(prefix + "type", move.getType());
            data.setProperty(prefix + "maxPP", Integer.toString(move.getMaxPP()));
            data.setProperty(prefix + "currentPP", Integer.toString(move.getCurrentPP()));
        }

        Path tempPath = savePath.resolveSibling(savePath.getFileName() + ".tmp");
        try {
            Path parent = savePath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            try (OutputStream output = Files.newOutputStream(tempPath)) {
                data.store(output, "Pokemon game progress");
            }
            try {
                Files.move(tempPath, savePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempPath, savePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar o progresso: " + e.getMessage(), e);
        }
    }

    public Optional<Pokemon> load() {
        if (!hasSave()) return Optional.empty();
        Properties data = new Properties();
        try (InputStream input = Files.newInputStream(savePath)) {
            data.load(input);
            int id = integer(data, "pokemon.id");
            Pokemon pokemon = pokemonRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Pokémon salvo não existe no catálogo: " + id));
            int moveCount = integer(data, "moves.count");
            if (moveCount < 0 || moveCount > 4) throw new IllegalStateException("Save inválido: quantidade de golpes.");
            List<Move> moves = new ArrayList<>();
            for (int i = 0; i < moveCount; i++) {
                String prefix = "moves." + i + ".";
                Move move = new Move(required(data, prefix + "name"), integer(data, prefix + "power"),
                        integer(data, prefix + "accuracy"), required(data, prefix + "type"),
                        integer(data, prefix + "maxPP"));
                move.restoreCurrentPP(integer(data, prefix + "currentPP"));
                moves.add(move);
            }
            pokemon.restoreProgress(integer(data, "pokemon.level"), integer(data, "pokemon.experience"),
                    integer(data, "pokemon.currentHp"), integer(data, "pokemon.maxHp"),
                    integer(data, "pokemon.attack"), integer(data, "pokemon.defense"),
                    integer(data, "pokemon.speed"), moves);
            return Optional.of(pokemon);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Não foi possível carregar o save: " + e.getMessage(), e);
        }
    }

    public void reset() {
        try {
            Files.deleteIfExists(savePath);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível resetar o progresso: " + e.getMessage(), e);
        }
    }

    private int integer(Properties data, String key) {
        return Integer.parseInt(required(data, key));
    }

    private String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Save incompleto: " + key);
        return value;
    }
}
