package view;

import infra.AsciiArtRepository;
import java.util.Scanner;

public class IntroScreen {
    private final AsciiArtRepository asciiRepo;

    public IntroScreen(AsciiArtRepository asciiRepo) {
        this.asciiRepo = asciiRepo;
    }

    public void show(Scanner input) {
        System.out.println(asciiRepo.getAsciiArt("INTRO"));
        System.out.println("Pressione Enter para continuar...");
        if (input.hasNextLine()) {
            input.nextLine();
        }
    }
}
