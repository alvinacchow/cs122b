public class Main {
    public static void main(String[] args) {
        MovieParser movie = new MovieParser();
        movie.run();
        ActorParser actor = new ActorParser();
        actor.run();
        StarParser star = new StarParser();
        star.run();
    }
}