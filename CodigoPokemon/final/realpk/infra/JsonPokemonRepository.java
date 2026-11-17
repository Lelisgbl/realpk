package infra;

import model.FirePokemon;
import model.GrassPokemon;
import model.GenericPokemon;
import model.Move;
import model.Pokemon;
import model.WaterPokemon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonPokemonRepository implements PokemonRepository {
    private final List<Pokemon> cachePokemons = new ArrayList<>();

    public JsonPokemonRepository(String filePath) {
        carregarDadosDoArquivo(filePath);
    }

    private void carregarDadosDoArquivo(String filePath) {
        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = abrirReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) json.append(line).append('\n');
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler o arquivo de Pokémons: " + e.getMessage(), e);
        }
        try {
            parseJsonToObjects(removeComments(json.toString()));
        } catch (RuntimeException e) {
            throw new IllegalStateException("Catálogo de Pokémon inválido. Confira IDs, tipos, atributos e golpes em pokemons.json.", e);
        }
    }

    private String removeComments(String source) {
        StringBuilder result = new StringBuilder();
        boolean quoted = false, escaped = false;
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            if (quoted) {
                result.append(current);
                if (escaped) escaped = false;
                else if (current == '\\') escaped = true;
                else if (current == '"') quoted = false;
            } else if (current == '"') {
                quoted = true;
                result.append(current);
            } else if (current == '/' && i + 1 < source.length() && source.charAt(i + 1) == '/') {
                while (i < source.length() && source.charAt(i) != '\n') i++;
                result.append('\n');
            } else if (current == '/' && i + 1 < source.length() && source.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < source.length() && !(source.charAt(i) == '*' && source.charAt(i + 1) == '/')) {
                    if (source.charAt(i) == '\n') result.append('\n');
                    i++;
                }
                i++;
            } else result.append(current);
        }
        return result.toString();
    }

    private BufferedReader abrirReader(String filePath) throws IOException {
        InputStream input = getClass().getClassLoader().getResourceAsStream(filePath);
        if (input != null) return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        return new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8));
    }

    private void parseJsonToObjects(String json) {
        for (String block : extractObjects(json)) {
            int id = intValue(block, "id", 0);
            String name = stringValue(block, "name");
            String type = stringValue(block, "type");
            int level = intValue(block, "level", 5);
            int nextId = intValue(block, "evolutionId", 0);
            int hp = intValue(block, "hp", 1);
            int attack = intValue(block, "attack", 1);
            int defense = intValue(block, "defense", 1);
            int speed = intValue(block, "speed", 1);
            Pokemon pokemon = switch (type.toUpperCase()) {
                case "FIRE" -> new FirePokemon(name, level, hp, attack, defense, speed);
                case "WATER" -> new WaterPokemon(name, level, hp, attack, defense, speed);
                case "GRASS" -> new GrassPokemon(name, level, hp, attack, defense, speed);
                default -> new GenericPokemon(name, type, level, hp, attack, defense, speed);
            };
            pokemon.setIdentity(id, nextId);
            Matcher secondaryType = Pattern.compile("\\\"secondaryType\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").matcher(block);
            if (secondaryType.find()) pokemon.setSecondaryType(secondaryType.group(1));
            for (String moveBlock : extractObjects(arrayValue(block, "moves"))) {
                pokemon.addMove(new Move(stringValue(moveBlock, "name"),
                        intValue(moveBlock, "power", 0), intValue(moveBlock, "accuracy", 100),
                        stringValue(moveBlock, "type"), intValue(moveBlock, "maxPP", 10)));
            }
            cachePokemons.add(pokemon);
        }
    }

    private List<String> extractObjects(String json) {
        List<String> objects = new ArrayList<>();
        boolean quoted = false;
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) quoted = !quoted;
            if (quoted) continue;
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && --depth == 0 && start >= 0) objects.add(json.substring(start, i + 1));
        }
        return objects;
    }

    private String stringValue(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (!m.find()) throw new IllegalArgumentException("Campo ausente: " + key);
        return m.group(1);
    }

    private int intValue(String json, String key, int fallback) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : fallback;
    }

    private String arrayValue(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[").matcher(json);
        if (!m.find()) return "[]";
        int start = m.end() - 1, depth = 0;
        boolean quoted = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) quoted = !quoted;
            if (quoted) continue;
            if (c == '[') depth++;
            else if (c == ']' && --depth == 0) return json.substring(start, i + 1);
        }
        throw new IllegalArgumentException("Array inválido: " + key);
    }

    @Override public List<Pokemon> findAll() {
        return cachePokemons.stream().map(Pokemon::copy).toList();
    }
    @Override public Optional<Pokemon> findByName(String name) {
        return cachePokemons.stream().filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst().map(Pokemon::copy);
    }
    @Override public Optional<Pokemon> findById(int id) {
        return cachePokemons.stream().filter(p -> p.getId() == id).findFirst().map(Pokemon::copy);
    }
}
