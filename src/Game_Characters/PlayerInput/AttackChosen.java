package Game_Characters.PlayerInput;

import java.util.List;

import Game_Characters.Enemy;
import Game_Characters.Player;

//Methods List:
// - attackLogic(float modifier):
// - checkAndRemoveDefeatedEnemies():

//Abstract Methods (Implemented):
// - initializeOptions():

//Inner Classes:
// - SlashAttack:
//   - execute():
// - FireAttack:
//   - execute():

public class AttackChosen extends Interaction {
    
    private Player player;
    private List<Enemy> enemies;
    private Enemy targetEnemy;
    private AttackInteraction parentBattle;
    
    //constructor
    public AttackChosen(Player player, List<Enemy> enemies, AttackInteraction parentBattle) {
        super("Attack Chosen", "You have chosen to attack!");
        this.player = player;
        this.enemies = enemies;
        this.targetEnemy = enemies.get(0); // Default to first enemy
        this.parentBattle = parentBattle;
    }

    //Overrides
    @Override
    protected void initializeOptions() {
        // Implementation for AttackChosen interaction options
        addOption(new SlashAttack());
        addOption(new FireAttack());
    }

    public class SlashAttack extends Option {
        public SlashAttack() {
            super("Perform a Slash Attack");
        }

        @Override
        public Interaction execute() {
            //option text
            System.out.println("\nYou perform a powerful slash attack!");

            //Unique attack logic
            targetEnemy = enemies.get(0); // Always target the first enemy
            attackLogic(1.0f);

            // Check all enemies for defeat and award exp
            checkAndRemoveDefeatedEnemies();

            // All enemies counter-attack (if any remain)
            if (!enemies.isEmpty()) {
                parentBattle.triggerEnemiesTurn();
            }

            // Return to parent battle to check status
            return parentBattle.checkStatus();
        }
    }

    public class FireAttack extends Option {
        public FireAttack() {
            super("Perform a Fire Attack");
        }

        @Override
        public Interaction execute() {
            //Option text
            System.out.println("\nYou cast a blazing fire attack! All enemies hit!");

            //Unique attack logic
            for (Enemy enemy : enemies) {
                targetEnemy = enemy;
                attackLogic(0.5f);
            }

            // Check all enemies for defeat and award exp
            checkAndRemoveDefeatedEnemies();

            // All enemies counter-attack (if any remain)
            if (!enemies.isEmpty()) {
                parentBattle.triggerEnemiesTurn();
            }

            // Return to parent battle to check status
            return parentBattle.checkStatus();
        }
    }

    private void attackLogic(float modifier) {
        System.out.println("\n>>> " + player.getName() + " attacks " + targetEnemy.getName() + "!");
        int playerDamage = (int)(player.dealDamage() * modifier);
        targetEnemy.takeDamage(playerDamage);
    }

    private void checkAndRemoveDefeatedEnemies() {
        // Use iterator to safely remove while iterating
        enemies.removeIf(enemy -> {
            if (!enemy.getAlive()) {
                System.out.println(">>> " + enemy.getName() + " has been defeated!");
                // Award experience and gold (demonstrates concurrent resource access)
                parentBattle.addExpGained(enemy.getExpOnDefeat());
                parentBattle.addGoldGained(enemy.getGoldValue());
                // Record enemy defeat in ArrayList (functional enhancement)
                player.recordEnemyDefeated(enemy.getName());
                return true; // Remove this enemy
            }
            return false; // Keep this enemy
        });
    }


    
}
