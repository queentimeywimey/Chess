import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.Posn;

public class ChessWorld extends World{
  Board board = new Board();
  Player current;
  Player next;
  boolean check = false;
  Selected selected = new Empty();
  int tick = 0;
  
  
  ChessWorld(){
    this.current = new Human(BW.WHITE);
    this.next = new Computer(BW.BLACK);
  }
  
  public static void main(String[] args) {
    ChessWorld eGame = new ChessWorld(new Human(BW.WHITE), new Human(BW.BLACK));
    eGame.bigBang(8*Draw.tileSize, 8*Draw.tileSize, 0.1);
  }
  
  ChessWorld(Player current, Player next){
    if (!current.getColor().equals(BW.WHITE) || !next.getColor().equals(BW.BLACK)) {
      throw new IllegalArgumentException("at least one of your players is the wrong color");
    }
    this.current = current;
    this.next = next;
  }
  
  // custom
  ChessWorld(Board board){
    this.board = new Board();
  }

  public WorldScene makeScene() {
    WorldScene boardScene = Draw.newBoardScene();
    Draw.placePieces(this.board.getPieces(), boardScene);
    Draw.drawActions(this.selected.getActions(), boardScene);
    if (check) {
      Draw.checked(this.board.findKing(this.current.color), boardScene);
    }
    Draw.selected(selected.getCoords(), boardScene);
    return boardScene;
  }

  public void onMouseClicked(Posn click){
    if (this.current.isHuman()) {
      Posn coord = clickToGame(click);
      this.play(coord);
    }
  }

  public void onTick() {
    this.current.generateAction(this.board.getPieces()).ifPresent(a -> this.turn(a));
    tick++;
  }

  public WorldScene lastScene(String msg) {
    WorldScene boardScene = Draw.newBoardScene();
    Draw.placePieces(this.board.getPieces(), boardScene);
    Draw.drawActions(this.selected.getActions(), boardScene);
    Draw.checked(this.board.findKing(this.next.color), boardScene);
    Draw.win(msg, boardScene);
    return boardScene;
  }
  
  public boolean play(Posn coord) {
    if (this.selected.getActions().containsKey(coord)) {
      this.turn(this.selected.getActions().get(coord));
      return true;
    } else if (this.board.getPieces().containsKey(coord)) {
      Piece selectedPiece = this.board.getPieces().get(coord);
      if (this.current.color.equals(selectedPiece.getColor())) {
        this.selected = new Exists(coord, this.board.getPieces());
      } else {
        this.selected = new Empty();
      }
    } else {
      this.selected = new Empty();
    }
    return false;
  }
  
  public void turn(Action toExecute) {
    this.board.unEnPassantifyAll();
    this.board.setPieces(toExecute.execute(this.board.getPieces()));
    this.selected = new Empty();
    this.check = this.board.inCheck(this.next.getColor());
    if (this.check && this.board.checkmate(this.next.getColor())) {
      this.endOfWorld(current + " wins!");
    }
    this.switchPlayers();
  }

  void switchPlayers() {
    Player saveCurrent = this.current;
    this.current = this.next;
    this.next = saveCurrent;
  }

  static Posn clickToGame(Posn click) {
    return new Posn(click.x/Draw.tileSize, click.y/Draw.tileSize);
  }
}