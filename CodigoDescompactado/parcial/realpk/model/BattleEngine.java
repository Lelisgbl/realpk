package model;

import infra.PokemonRepository;

public class BattleEngine {
    private final Pokemon playerPokemon;
    private final Pokemon opponentPokemon;
    private final PokemonRepository pokemonRepository;
    private int currentTurn = 1;
    private boolean rewardsGranted;

    public BattleEngine(Pokemon playerPokemon, Pokemon opponentPokemon) {
        this(playerPokemon, opponentPokemon, null);
    }

    public BattleEngine(Pokemon playerPokemon, Pokemon opponentPokemon, PokemonRepository pokemonRepository) {
        this.playerPokemon = playerPokemon;
        this.opponentPokemon = opponentPokemon;
        this.pokemonRepository = pokemonRepository;
    }

    public void executeTurn(int playerMoveIndex, int opponentMoveIndex) {
        if (isBattleOver()) return;
        System.out.println("\n--- Turno " + currentTurn + " ---");
        if (playerPokemon.getSpeed() >= opponentPokemon.getSpeed()) {
            playerPokemon.attack(playerMoveIndex, opponentPokemon);
            if (!opponentPokemon.isFainted()) opponentPokemon.attack(opponentMoveIndex, playerPokemon);
        } else {
            opponentPokemon.attack(opponentMoveIndex, playerPokemon);
            if (!playerPokemon.isFainted()) playerPokemon.attack(playerMoveIndex, opponentPokemon);
        }
        currentTurn++;
        grantWinnerReward();
    }

    private void grantWinnerReward() {
        if (rewardsGranted || !isBattleOver()) return;
        rewardsGranted = true;
        if (checkWinner() == playerPokemon) {
            int reward = Math.max(50, opponentPokemon.getLevel() * 50);
            System.out.println(playerPokemon.getName() + " ganhou " + reward + " de experiência!");
            int levels = playerPokemon.gainExperience(reward);
            while (levels-- > 0 && pokemonRepository != null
                    && playerPokemon.getNextEvolutionId() > 0) {
                pokemonRepository.findById(playerPokemon.getNextEvolutionId())
                        .ifPresent(playerPokemon::evolveTo);
            }
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