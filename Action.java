import java.awt.Color;
import java.util.HashMap;
import javalib.worldimages.CircleImage;
import javalib.worldimages.Posn;
import javalib.worldimages.WorldImage;

abstract class Action{
  Piece movePiece;
  Posn to;
  
  Action(Posn to, Piece movePiece){
    this.to = to;
    this.movePiece = movePiece;
  }

  abstract WorldImage getImage();

  public HashMap<Posn, Piece> execute(HashMap<Posn, Piece> pieces) {
    pieces = new HashMap<Posn, Piece>(pieces);
    pieces.remove(this.movePiece.coords);
    if (this.to.y == this.movePiece.getColor().finalRow()
        && this.movePiece.isPawn()) {
      pieces.put(this.to, new Queen(this.to, this.movePiece.getColor()));
    } else {
      pieces.put(this.to, movePiece.cloneDiffCoords(this.to));
    }
    return pieces;
  }
}

class Move extends Action{
  static final WorldImage image = new CircleImage(Draw.tileSize/2 - 10, "outline", Color.CYAN);
  
  Move(Posn to, Piece movePiece){
    super(to, movePiece);
  }

  public WorldImage getImage() {
    return image;
  }
}

class Capture extends Action{
  static final WorldImage image = new CircleImage(Draw.tileSize/2 - 10, "outline", Color.MAGENTA);
  
  Capture(Posn to, Piece movePiece){
    super(to, movePiece);
  }

  public WorldImage getImage() {
    return image;
  }
  
  public HashMap<Posn, Piece> execute(HashMap<Posn, Piece> pieces) {
    if (!pieces.get(this.to).isKing()) {
      return super.execute(pieces);
    } else {
      return pieces;
    }
  }
}

class EnPassant extends Capture{
  
  EnPassant(Posn to, Piece movePiece) {
    super(to, movePiece);
  }

  public HashMap<Posn, Piece> execute(HashMap<Posn, Piece> pieces) {
    pieces = new HashMap<Posn, Piece>(pieces);
    pieces.remove(movePiece.getCoords());
    pieces.remove(new Posn(this.to.x, movePiece.getCoords().y));
    pieces.put(this.to, movePiece.cloneDiffCoords(this.to));
    return pieces;
  }
}

class Castle extends Move{
  Move rookMove;

  Castle(Piece king, Piece rook, RL side){
    super(new Posn(side.kingToX(), king.getCoords().y), king);
    this.rookMove = new Move(new Posn(side.rookToX(), rook.getCoords().y), rook);
  }
  
  public HashMap<Posn, Piece> execute(HashMap<Posn, Piece> pieces) {
    return super.execute(rookMove.execute(pieces));
  }
}