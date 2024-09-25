import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

import javalib.worldimages.Posn;

abstract class Player {
  BW color;
  
  Player(BW color){
    this.color = color;
  }

  public BW getColor() {
    return this.color;
  }
  
  public String toString() {
    return this.color.stringify();
  }
  
  public abstract Optional<Action> generateAction(HashMap<Posn, Piece> pieces);

  protected abstract boolean isHuman();
}

class Human extends Player{

  Human(BW color) {
    super(color);
  }

  public Optional<Action> generateAction(HashMap<Posn, Piece> pieces) {
    return Optional.empty();
  }

  protected boolean isHuman() {
    return true;
  }
  
}

class Computer extends Player{
  
  Computer(BW color){
    super(color);
  }

  public Optional<Action> generateAction(HashMap<Posn, Piece> pieces) {
    return Optional.of(this.nextMove(pieces));
  }

  private Action nextMove(HashMap<Posn, Piece> pieces) {
    ArrayList<Action> allActions = new ArrayList<Action>();
    for (Piece piece : pieces.values()) {
      if (piece.getColor().equals(this.color)) {
        allActions.addAll(piece.generateActions(pieces).values());
      }
    }
    return allActions.get(new Random().nextInt(allActions.size()));
  }

  protected boolean isHuman() {
    return false;
  }

}