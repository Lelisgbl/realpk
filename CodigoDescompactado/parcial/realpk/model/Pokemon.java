package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Pokemon {
    protected int id;
    protected String name;
    protected String type;
    protected String secondaryType = "";
    protected int level;
    protected int experience;
    protected int nextEvolutionId;
    protected int maxHp;
    protected int currentHp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected List<Move> moves;

    public Pokemon(String name, String type, int level, int hp, int attack, int defense, int speed) {
        this(name, type, level, hp, attack, defense, speed, 0, 0);
    }

    public Pokemon(String name, String type, int level, int hp, int attack, int defense, int speed,
                   int id, int nextEvolutionId) {
        this.id = id; this.name = name; this.type = type; this.level = level;
        this.maxHp = hp; this.currentHp = hp; this.attack = attack;
        this.defense = defense; this.speed = speed; this.nextEvolutionId = nextEvolutionId;
        this.moves = new ArrayList<>();
    }

    public abstract void performSpecialAbility();

    public Pokemon copy() {
        Pokemon clone = switch (type.toUpperCase()) {
            case "FIRE" -> new FirePokemon(name, level, maxHp, attack, defense, speed);
            case "WATER" -> new WaterPokemon(name, level, maxHp, attack, defense, speed);
            case "GRASS" -> new GrassPokemon(name, level, maxHp, attack, defense, speed);
            default -> new GenericPokemon(name, type, level, maxHp, attack, defense, speed);
        };
        clone.id = id;
        clone.experience = experience;
        clone.nextEvolutionId = nextEvolutionId;
        clone.secondaryType = secondaryType;
        clone.currentHp = currentHp;
        for (Move move : moves) {
            clone.addMove(new Move(move.getName(), move.getPower(), move.getAccuracy(),
                    move.getType(), move.getMaxPP()));
        }
        return clone;
    }

    public void setIdentity(int id, int nextEvolutionId) {
        this.id = id;
        this.nextEvolutionId = nextEvolutionId;
    }

    public void setSecondaryType(String secondaryType) {
        this.secondaryType = secondaryType == null ? "" : secondaryType.trim();
    }

    public boolean attack(int moveIndex, Pokemon target) {
        if (moveIndex < 0 || moveIndex >= moves.size()) {
            System.out.println("Movimento inválido!");
            return false;
        }
        Move move = moves.get(moveIndex);
        if (!move.useMove()) {
            System.out.println(move.getName() + " não tem mais PP!");
            return false;
        }
        double multiplier = TypeEffectiveness.multiplier(move.getType(), target);
        int rawDamage = Math.max(1, attack + move.getPower() - target.getDefense() / 2);
        int damage = (int) Math.round(rawDamage * multiplier);
        System.out.println(name + " usou " + move.getName() + "!");
        if (multiplier == 0.0) {
            System.out.println(TypeEffectiveness.describe(multiplier));
        } else {
            target.takeDamage(damage);
            if (multiplier != 1.0) {
                System.out.println(TypeEffectiveness.describe(multiplier) + " (" + damage + " de dano)");
            }
        }
        return true;
    }

    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - Math.max(0, damage));
        System.out.println(name + " recebeu " + damage + " de dano. HP restante: " + currentHp + "/" + maxHp);
    }

    public int gainExperience(int amount) {
        if (amount <= 0) return 0;
        experience += amount;
        int levelsGained = 0;
        while (experience >= experienceToNextLevel()) {
            experience -= experienceToNextLevel();
            level++;
            maxHp += 5;
            currentHp = Math.min(maxHp, currentHp + 5);
            attack += 2;
            defense += 1;
            speed += 1;
            levelsGained++;
            System.out.println(name + " subiu para o nível " + level + "!");
        }
        return levelsGained;
    }

    private int experienceToNextLevel() { return level * 100; }

    public void evolveTo(Pokemon form) {
        if (form == null) return;
        int hpIncrease = Math.max(0, form.maxHp - maxHp);
        maxHp += hpIncrease;
        currentHp = Math.min(maxHp, currentHp + hpIncrease);
        attack += Math.max(0, form.attack - attack);
        defense += Math.max(0, form.defense - defense);
        speed += Math.max(0, form.speed - speed);
        id = form.id; name = form.name; type = form.type; secondaryType = form.secondaryType;
        nextEvolutionId = form.nextEvolutionId;
        for (Move move : form.moves) {
            boolean known = moves.stream().anyMatch(m -> m.getName().equalsIgnoreCase(move.getName()));
            if (!known && moves.size() < 4) {
                moves.add(new Move(move.getName(), move.getPower(), move.getAccuracy(), move.getType(), move.getMaxPP()));
                System.out.println(name + " aprendeu " + move.getName() + "!");
            }
        }
        System.out.println("Seu Pokémon evoluiu para " + name + "!");
    }

    public boolean isFainted() { return currentHp <= 0; }
    public boolean addMove(Move move) { if (moves.size() < 4) { moves.add(move); return true; } return false; }
    public float getHpPercentage() { return maxHp == 0 ? 0 : ((float) currentHp / maxHp) * 100; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public List<String> getTypes() {
        return secondaryType.isBlank() ? List.of(type) : List.of(type, secondaryType);
    }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExperienceToNextLevel() { return experienceToNextLevel(); }
    public int getNextEvolutionId() { return nextEvolutionId; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public List<Move> getMoves() { return new ArrayList<>(moves); }
}
