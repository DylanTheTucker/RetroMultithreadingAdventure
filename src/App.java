import GameHandlers.GameManager;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("=== The Knight's Journey ===\n");
        
        // Create and start the game manager
        GameManager gameManager = new GameManager();
        gameManager.start();
        
        System.out.println("\n=== Journey Complete ===");
    }
}
