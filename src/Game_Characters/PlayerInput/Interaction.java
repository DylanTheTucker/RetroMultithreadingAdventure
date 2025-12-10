package Game_Characters.PlayerInput;

import java.util.ArrayList;
import java.util.List;

// An abstract class that streamlines all prompts for player input

//Methods List:
// - addOption(Option option):
// - display():
// - getOption(int index):
// - getOptionCount():

//Abstract Methods (Not Implemented):
// - initializeOptions():  sets up all options in the interaction

//Getters:
// - isComplete():

//Setters:
// - setComplete(boolean complete):

//Inner Abstract Class:
// - Option:
//   - getDescription():  option name
//   - execute():  executes the option logic
//   - setNextInteraction(Interaction next):

//Inner class represents options unique to the interaction, and extends Option for shared behavior

public abstract class Interaction {
    
    protected String title;
    protected String description;
    protected List<Option> options;
    protected boolean isComplete;
    
    public Interaction(String title, String description) {
        this.title = title;
        this.description = description;
        this.options = new ArrayList<>();
        this.isComplete = false;
        initializeOptions();
    }
    
    /**
     * Override this to set up the options for this interaction.
     * Call addOption() for each option you want to add.
     */
    protected abstract void initializeOptions();
    
    /**
     * Adds an option to this interaction
     */
    protected void addOption(Option option) {
        options.add(option);
    }
    
    /**
     * Displays the interaction to the player
     */
    public void display() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(title);
        System.out.println("=".repeat(50));
        System.out.println(description);
        System.out.println();
        
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + ": " + options.get(i).getDescription());
        }
    }
    
    /**
     * Gets the option at the specified index
     */
    public Option getOption(int index) {
        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }
        return null;
    }
    
    /**
     * Gets the number of options
     */
    public int getOptionCount() {
        return options.size();
    }
    
    /**
     * Checks if this interaction has been completed
     */
    public boolean isComplete() {
        return isComplete;
    }
    
    /**
     * Marks this interaction as complete
     */
    public void setComplete(boolean complete) {
        this.isComplete = complete;
    }
    
    /**
     * Abstract nested class for options.
     * Each concrete Interaction subclass should define its own Option subclasses.
     */
    public abstract class Option {
        protected String description;
        protected Interaction nextInteraction;
        
        public Option(String description) {
            this.description = description;
            this.nextInteraction = null;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * Execute the logic for this option.
         * @return The next Interaction to show, or null if interaction ends
         */
        public abstract Interaction execute();
        
        /**
         * Sets the next interaction to show after this option is executed
         */
        public void setNextInteraction(Interaction next) {
            this.nextInteraction = next;
        }
    }
}

