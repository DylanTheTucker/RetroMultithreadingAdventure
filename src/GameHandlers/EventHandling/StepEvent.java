package GameHandlers.EventHandling;

import GameHandlers.BattleManager;
import Game_Characters.PlayerInput.Interaction;

//Sets up events that occur on certain steps

//Abstract Methods (Not Implemented):
// - performEvent(BattleManager battleManager):  

//Getters:
// - getInteraction():
// - hasInteraction():

public abstract class StepEvent {
    
    protected Interaction interaction;
    
    /**
     * Performs the event logic.
     * @param battleManager The battle manager for combat events
     */
    public abstract void performEvent(BattleManager battleManager);
    
    /**
     * Gets the interaction for this event, if one exists.
     * @return The Interaction, or null if this event has no interaction
     */
    public Interaction getInteraction() {
        return interaction;
    }
    
    /**
     * Checks if this event has an interaction component.
     * @return true if interaction exists, false otherwise
     */
    public boolean hasInteraction() {
        return interaction != null;
    }
}
