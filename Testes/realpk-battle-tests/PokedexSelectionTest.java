import infra.JsonPokemonRepository;
import infra.AsciiArtRepository;
import model.Pokemon;
import view.PokedexScreen;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class PokedexSelectionTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        JsonPokemonRepository repository = new JsonPokemonRepository("CodigoDescompactado/parcial/realpk/pokemons.json");
        PokedexScreen screen = new PokedexScreen(repository);
        AsciiArtRepository artRepository = new AsciiArtRepository("CodigoDescompactado/parcial/realpk/ascii_arts.txt");
        for (Pokemon pokemon : repository.findAll()) {
            check(!artRepository.getAsciiArt(pokemon.getName()).contains("[Desconhecido]"),
                    "missing ASCII art for " + pokemon.getName());
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        System.setOut(new PrintStream(output));
        try {
            screen.displayCatalog();
            Pokemon selected = screen.choosePokemon(new Scanner("2\n999\n4\n"));
            check(selected != null && selected.getId() == 4, "valid selection after invalid ID failed");
            check(screen.choosePokemon(new Scanner("0\n")) == null, "cancel selection failed");
        } finally {
            System.setOut(previous);
        }
        String catalog = output.toString();
        check(catalog.contains("POKÉDEX") && catalog.contains("#001 Bulbasaur")
                && catalog.contains("#066 Machop") && catalog.contains("#016 Pidgey")
                && catalog.contains("#002 Ivysaur") && catalog.contains("#068 Machamp"), "full catalog is not displayed");
        check(catalog.contains("Número inválido"), "invalid selection feedback missing");
        System.out.println("POKEDEX_ACCESS_AND_SELECTION_TESTS_OK");
    }
}
