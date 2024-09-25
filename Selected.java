import java.util.HashMap;
import java.util.Optional;
import javalib.worldimages.Posn;

interface Selected {
  public Optional<Posn> getCoords();
  
  public HashMap<Posn, Action> getActions();
}

class Exists implements Selected{
  Posn selectedCoords;
  HashMap<Posn, Action> actions;
  
  Exists(Posn selectedCoords, HashMap<Posn, Piece> pieces){
    this.selectedCoords = selectedCoords;
    this.actions = pieces.get(selectedCoords).generateActions(pieces);
  }

  public Optional<Posn> getCoords() {
    return Optional.of(this.selectedCoords);
  }

  public HashMap<Posn, Action> getActions() {
    return this.actions;
  }
}

class Empty implements Selected{

  public Optional<Posn> getCoords() {
    return Optional.empty();
  }

  public HashMap<Posn, Action> getActions() {
    return new HashMap<Posn, Action>();
  }
  
  
}