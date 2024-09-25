import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javalib.worldimages.Posn;

class Board{
  HashMap<Posn, Piece> pieces = new HashMap<Posn, Piece>();
  
  Board(){
    for (Piece p : Board.generateStarters()) {
      this.pieces.put(p.getCoords(), p);
    }
  }
  
  public void unEnPassantifyAll() {
    for (Piece p : this.pieces.values()) {
      p.unEnPassantify();
    }
  }

  public boolean checkmate(BW color) {
    return this.pieces.values().stream()
        .filter(p -> p.getColor().equals(color))
        .map(p -> p.generateActions(this.pieces))
        .allMatch(HashMap :: isEmpty);
  }

  Board(HashMap<Posn, Piece> pieces){
    this.pieces = pieces;
  }

  public void setPieces(HashMap<Posn, Piece> pieces) {
    this.pieces = pieces;
  }

  private static List<Piece> generateStarters() {
    List<Piece> toReturn = new ArrayList<Piece>();
        
    for (BW color : BW.values()) {
      toReturn.add(new King(color));
      toReturn.add(new Queen(color));
      for (RL side : RL.values()) {
        toReturn.add(new Bishop(color, side));
        toReturn.add(new Knight(color, side));
        toReturn.add(new Rook(color, side));
      }
      for(int i = 0; i < 8; i++) {
        toReturn.add(new Pawn(color, i));
      }
    }
    
    return toReturn; 
  }

  public HashMap<Posn, Piece> getPieces() {
    return this.pieces;
  }

  public boolean inCheck(BW color) {
    Posn kingPosn = this.findKing(color);
    
    for (Piece p : pieces.values()) {
      if (!p.getColor().equals(color) && p.generateCaptures(this.pieces).keySet().stream().anyMatch(po -> po.equals(kingPosn))) {
        return true;
      }
    }
    return false;
  }

  public Posn findKing(BW color) {
    for (Piece p : this.pieces.values()) {
      if (p.rightKing(color)) {
        return p.getCoords();
      }
    }
    throw new RuntimeException("why is your king not here, dude");
  }
}