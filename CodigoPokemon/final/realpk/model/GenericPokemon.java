package model;

/** Pokémon whose primary type does not have a dedicated battle subclass. */
public final class GenericPokemon extends Pokemon {
    public GenericPokemon(String name, String type, int level, int hp, int attack, int defense, int speed) {
        super(name, type, level, hp, attack, defense, speed);
    }

    @Override
    public void performSpecialAbility() {
        System.out.println(name + " não possui habilidade especial implementada.");
    }
}
