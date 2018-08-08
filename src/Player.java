import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

interface GameStrategy{

    default void processDraftTurn(GameState currentGame){
        Optional<Card> maxByValue = currentGame.myPlayer.cardsInHand.stream().max(Comparator.comparing(Card::getValue));
        maxByValue.ifPresent(card -> System.out.println("PICK " + card.position));
    }

    void processBattleTurn(GameState currentGame);
}




class DefaultStrategy implements GameStrategy{

    boolean decideAttack(GameState currentGame){
        String action = "";
        int target = -1;
        Optional<Card> anyBlueItem = currentGame.myPlayer.cardsInHand.stream()
                .filter(card -> card.cardType == 3 && card.cost <= currentGame.myPlayer.remainingMana)
                .findAny();
        if(anyBlueItem.isPresent()) {
            action += "USE " + anyBlueItem.get().instanceId + " -1;";
            currentGame.myPlayer.remainingMana -= anyBlueItem.get().cost;
            currentGame.myPlayer.cardsInHand.remove(anyBlueItem.get());
        }
        Optional<Card> guard = currentGame.opponent.cardsOnBoard.stream()
                .filter(card -> card.abilities.contains("G"))
                .findAny();
        if(guard.isPresent()){
            target = guard.get().instanceId;
        }

        Optional<Card> maxByValue = currentGame.myPlayer.cardsOnBoard.stream()
                .filter(card -> card.attack>0 && card.cardType == 0 && card.cost <= currentGame.myPlayer.remainingMana)
                .max(Comparator.comparing(Card::getValue));


        if(maxByValue.isPresent()){
            Optional<Card> anyGreenItem = currentGame.myPlayer.cardsInHand.stream()
                    .filter(card -> card.attack>0 && card.cardType == 1 && card.cost <= currentGame.myPlayer.remainingMana - maxByValue.get().cost)
                    .findAny();
            if(anyGreenItem.isPresent()) {
                action += "USE " + anyGreenItem.get().instanceId + " " + maxByValue.get().instanceId + ";";
                currentGame.myPlayer.remainingMana -= anyGreenItem.get().cost;
                currentGame.myPlayer.cardsInHand.remove(anyGreenItem.get());
            }
            action += "ATTACK "+maxByValue.get().instanceId+" "+target+";";
            currentGame.myPlayer.remainingMana -= maxByValue.get().cost;
            if(guard.isPresent() && maxByValue.get().attack >= guard.get().defense)
                currentGame.opponent.cardsOnBoard.remove(guard.get());
            currentGame.myPlayer.cardsOnBoard.remove(maxByValue.get());
        }
        currentGame.myPlayer.action += action;
        return !"".equals(action);
    }

    @Override
    public void processBattleTurn(GameState currentGame) {
        currentGame.myPlayer.action = "";

        currentGame.myPlayer.remainingMana = currentGame.myPlayer.playerMana;
        boolean actionTaken = true;
        while(currentGame.myPlayer.remainingMana>0 && actionTaken) {
            actionTaken = decideAttack(currentGame);
        }

        actionTaken = true;
        while(currentGame.myPlayer.remainingMana>0 && actionTaken) {
            actionTaken = decideSummon(currentGame);
        }


        if(!"".equals(currentGame.myPlayer.action)){
            System.out.println(currentGame.myPlayer.action);
            System.err.print(+(currentGame.myPlayer.remainingMana));
        } else
            System.out.println("PASS");
    }

    private boolean decideSummon(GameState currentGame) {
        boolean action = false;
        System.err.println("enter(S): "+currentGame.myPlayer.remainingMana);
        Optional<Card> maxByValue = currentGame.myPlayer.cardsInHand.stream()
                .filter(card -> card.cardType == 0 && card.cost <= currentGame.myPlayer.remainingMana )
                .max(Comparator.comparing(Card::getValue));

        if(maxByValue.isPresent()){
            currentGame.myPlayer.action += "SUMMON "+maxByValue.get().instanceId+";";
            currentGame.myPlayer.remainingMana -= maxByValue.get().cost;
            if(maxByValue.get().abilities.contains("C"))
                currentGame.myPlayer.cardsOnBoard.add(maxByValue.get());
            currentGame.myPlayer.cardsInHand.remove(maxByValue.get());
            action = true;
        }
        System.err.println("exit(S): "+currentGame.myPlayer.remainingMana);
        return action;
    }
}
class GameState{
    PlayerState myPlayer = new PlayerState();
    PlayerState opponent = new PlayerState();
    int turnNumber;
}
class PlayerState{
    int playerHealth;
    int playerMana;
    int playerDeck;
    int playerRune;
    String action;
    int remainingMana;
    List<Card> cardsInHand = new ArrayList<>();
    List<Card> cardsOnBoard = new ArrayList<>();

}
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        GameState currentGame = new GameState();
        GameStrategy strategy = new DefaultStrategy();
        // game loop

        while (true) {
            ArrayList<Card> cards = new ArrayList<>();
            currentGame.turnNumber++;
            PlayerState currentPlayerState = currentGame.myPlayer;
            for (int i = 0; i < 2; i++) {
                currentPlayerState.playerHealth = in.nextInt();
                currentPlayerState.playerMana = in.nextInt();
                currentPlayerState.playerDeck = in.nextInt();
                currentPlayerState.playerRune = in.nextInt();
                currentPlayerState.cardsInHand.clear();
                currentPlayerState.cardsOnBoard.clear();
                currentPlayerState = currentGame.opponent;
            }
            System.err.println(currentGame.turnNumber+ " - Mana:"+currentGame.myPlayer.playerMana);
            int opponentHand = in.nextInt();
            int cardCount = in.nextInt();
            for (int i = 0; i < cardCount; i++) {
                int cardNumber = in.nextInt();
                int instanceId = in.nextInt();
                int location = in.nextInt();
                int cardType = in.nextInt();
                int cost = in.nextInt();
                int attack = in.nextInt();
                int defense = in.nextInt();
                String abilities = in.next();
                int myHealthChange = in.nextInt();
                int opponentHealthChange = in.nextInt();
                int cardDraw = in.nextInt();
                Card c = new Card(cardNumber, instanceId, location, cardType, cost, attack, defense, abilities, myHealthChange, opponentHealthChange, i);
                if (c.location == 0)
                    currentGame.myPlayer.cardsInHand.add(c);
                else if (c.location == 1)
                    currentGame.myPlayer.cardsOnBoard.add(c);
                else if (c.location == -1)
                    currentGame.opponent.cardsOnBoard.add(c);
                else
                    cards.add(c);
            }
            if(currentGame.turnNumber <= 30){
                strategy.processDraftTurn(currentGame);
            }else{
                strategy.processBattleTurn(currentGame);
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

           // System.out.println("PASS");
        }
    }
    
}

class Card{
    int cardNumber;
    int instanceId;
    int location;
    int cardType;
    int cost;
    int attack;
    int defense;
    String abilities;
    int position;
    int myHealthChange;
    int opponentHealthChange;
    
    Card(int cardNumber, int instanceId,int location, int cardType,int cost, int attack, int defense, String abilities, int myHealthChange,int opponentHealthChange, int position){
        this.cardNumber = cardNumber;
        this.instanceId = instanceId;
        this.location = location;
        this.cardType = cardType;
        this.cost = cost;
        this.attack = attack;
        this.defense = defense;
        this.abilities = abilities;
        this.position = position;
        this.myHealthChange = myHealthChange;
        this.opponentHealthChange = opponentHealthChange;
    }
    
    double getValue(){
        if (this.cardType == 2 )
            return 0;
        return  ((double)2*this.attack
            + 2*this.myHealthChange
            - 2*this.opponentHealthChange)/
          //  + 2*((this.abilities.contains("B")||this.abilities.contains("G")||this.abilities.contains("D")||this.abilities.contains("L")||this.abilities.contains("W"))?1:0)))
             this.cost;
    }
    
    int getAttack(){
        return this.attack;
    }

    int getCost(){
        return this.cost;
    }


    public String toString(){
        return this.cardNumber + " " + this.location + " " + this.cardType + " " + this.cost;
    }

}