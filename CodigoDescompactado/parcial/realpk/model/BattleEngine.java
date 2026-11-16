package model;

import infra.PokemonRepository;
import view.BattleScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class BattleEngine {
    private final Pokemon playerPokemon;
    private final Pokemon opponentPokemon;
    private final PokemonRepository pokemonRepository;
    private final Random random;
    private int currentTurn = 1;
    private boolean rewardsGranted;

    public BattleEngine(Pokemon playerPokemon, Pokemon opponentPokemon) {
        this(playerPokemon, opponentPokemon, null);
    }

    public BattleEngine(Pokemon playerPokemon, Pokemon opponentPokemon, PokemonRepository pokemonRepository) {
        this(playerPokemon, opponentPokemon, pokemonRepository, new Random());
    }

    public BattleEngine(Pokemon playerPokemon, Pokemon opponentPokemon,
                        PokemonRepository pokemonRepository, Random random) {
        this.playerPokemon = playerPokemon;
        this.opponentPokemon = opponentPokemon;
        this.pokemonRepository = pokemonRepository;
        this.random = random;
    }

    public static Pokemon chooseRandomOpponent(List<Pokemon> catalog, Pokemon player, Random random) {
        List<Pokemon> differentType = catalog.stream()
                .filter(p -> p.getId() != player.getId())
                .filter(p -> !p.getTypes().equals(player.getTypes()))
                .toList();
        List<Pokemon> candidates = differentType.isEmpty()
                ? catalog.stream().filter(p -> p.getId() != player.getId()).toList()
                : differentType;
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("O catálogo precisa ter outro Pokémon para iniciar a batalha.");
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    public void startBattle(Scanner input, BattleScreen screen) {
        screen.displayMessage("A batalha começou!");
        while (!isBattleOver()) {
            if (!hasAvailableMove(playerPokemon) || !hasAvailableMove(opponentPokemon)) {
                screen.displayMessage("Um dos Pokémon ficou sem golpes disponíveis. A batalha foi encerrada.");
                break;
            }
            screen.renderBattleUI(playerPokemon, opponentPokemon);
            screen.renderMoves(playerPokemon);
            int playerMoveIndex = readPlayerMove(input, screen);
            if (playerMoveIndex < 0) {
                screen.displayMessage("Batalha encerrada antes do resultado.");
                return;
            }
            int opponentMoveIndex = chooseRandomMove(opponentPokemon);
            executeTurn(playerMoveIndex, opponentMoveIndex);
        }
        Pokemon winner = checkWinner();
        if (winner == null) screen.displayMessage("A batalha terminou sem vencedor.");
        else screen.displayMessage("Vencedor: " + winner.getName() + " (nível " + winner.getLevel() + ")!");
    }

    private int readPlayerMove(Scanner input, BattleScreen screen) {
        while (true) {
            screen.displayMessage("Digite o número do golpe:");
            if (!input.hasNextLine()) return -1;
            String answer = input.nextLine().trim();
            try {
                int index = Integer.parseInt(answer) - 1;
                List<Move> moves = playerPokemon.getMoves();
                if (index >= 0 && index < moves.size() && moves.get(index).getCurrentPP() > 0) return index;
            } catch (NumberFormatException ignored) {
                // Repete a pergunta para entrada não numérica.
            }
            screen.displayMessage("Escolha inválida ou golpe sem PP. Tente novamente.");
        }
    }

    private boolean hasAvailableMove(Pokemon pokemon) {
        return pokemon.getMoves().stream().anyMatch(move -> move.getCurrentPP() > 0);
    }

    private int chooseRandomMove(Pokemon pokemon) {
        List<Move> available = new ArrayList<>();
        List<Move> moves = pokemon.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i).getCurrentPP() > 0) available.add(moves.get(i));
        }
        return moves.indexOf(available.get(random.nextInt(available.size())));
    }

    public void executeTurn(int playerMoveIndex, int opponentMoveIndex) {
        if (isBattleOver()) return;
        System.out.println("\n--- Turno " + currentTurn + " ---");
        boolean playerFirst = playerPokemon.getSpeed() > opponentPokemon.getSpeed()
                || (playerPokemon.getSpeed() == opponentPokemon.getSpeed() && random.nextBoolean());
        if (playerFirst) {
            if (playerPokemon.attack(playerMoveIndex, opponentPokemon)) awardAttackExperience();
            if (!opponentPokemon.isFainted()) opponentPokemon.attack(opponentMoveIndex, playerPokemon);
        } else {
            opponentPokemon.attack(opponentMoveIndex, playerPokemon);
            if (!playerPokemon.isFainted() && playerPokemon.attack(playerMoveIndex, opponentPokemon)) {
                awardAttackExperience();
            }
        }
        currentTurn++;
        grantWinnerReward();
    }

    private void awardAttackExperience() {
        System.out.println(playerPokemon.getName() + " ganhou 25 de experiência pelo ataque!");
        applyExperience(25);
    }

    private void grantWinnerReward() {
        if (rewardsGranted || !isBattleOver()) return;
        rewardsGranted = true;
        if (checkWinner() == playerPokemon) {
            int reward = Math.max(50, opponentPokemon.getLevel() * 200);
            System.out.println(playerPokemon.getName() + " ganhou bônus de vitória: " + reward + " de experiência!");
            applyExperience(reward);
        }
    }

    private void applyExperience(int amount) {
        int levels = playerPokemon.gainExperience(amount);
        while (levels-- > 0 && pokemonRepository != null && playerPokemon.getNextEvolutionId() > 0) {
            pokemonRepository.findById(playerPokemon.getNextEvolutionId()).ifPresent(playerPokemon::evolveTo);
        }
    }

    public boolean isBattleOver() {
        return playerPokemon.isFainted() || opponentPokemon.isFainted();
    }

    public Pokemon checkWinner() {
        if (playerPokemon.isFainted() && !opponentPokemon.isFainted()) return opponentPokemon;
        if (opponentPokemon.isFainted() && !playerPokemon.isFainted()) return playerPokemon;
        return null;
    }

    public Pokemon getPlayerPokemon() { return playerPokemon; }
    public Pokemon getOpponentPokemon() { return opponentPokemon; }
    public int getCurrentTurn() { return currentTurn; }
}

