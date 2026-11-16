package view;

import infra.AsciiArtRepository;
import model.Move;
import model.Pokemon;

public class BattleScreen {
    private final AsciiArtRepository asciiArtRepository;

    public BattleScreen(AsciiArtRepository asciiArtRepository) {
        this.asciiArtRepository = asciiArtRepository;
    }

    public void renderBattleUI(Pokemon player, Pokemon opponent) {
        limparTela();
        System.out.println("==================================================================");
        System.out.println(" [INIMIGO] " + opponent.getName() + " (Lv. " + opponent.getLevel() + ")");
        System.out.println(" HP: [" + construirBarraHp(opponent.getCurrentHp(), opponent.getMaxHp()) + "] "
                + opponent.getCurrentHp() + "/" + opponent.getMaxHp());
        System.out.println(asciiArtRepository.getAsciiArt(opponent.getName()));
        System.out.println("------------------------------------------------------------------");
        System.out.println(" [JOGADOR] " + player.getName() + " (Lv. " + player.getLevel() + ")");
        System.out.println(" HP: [" + construirBarraHp(player.getCurrentHp(), player.getMaxHp()) + "] "
                + player.getCurrentHp() + "/" + player.getMaxHp());
        System.out.println(asciiArtRepository.getAsciiArt(player.getName()));
        System.out.println("==================================================================");
    }

    public void renderMoves(Pokemon player) {
        System.out.println("\n--- Escolha um golpe ---");
        int index = 1;
        for (Move move : player.getMoves()) {
            System.out.println(" [" + index++ + "] " + move.getName()
                    + " | Tipo: " + move.getType()
                    + " | Poder: " + move.getPower()
                    + " | PP: " + move.getCurrentPP() + "/" + move.getMaxPP());
        }
    }

    public void displayMessage(String message) {
        System.out.println("[BATALHA] " + message);
    }

    private String construirBarraHp(int currentHp, int maxHp) {
        int total = 20;
        int filled = (int) Math.round(((double) currentHp / maxHp) * total);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < total; i++) bar.append(i < filled ? "█" : "-");
        return bar.toString();
    }

    private void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}