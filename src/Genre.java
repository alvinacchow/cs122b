import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;

public class Genre {
    private Integer id;
    private String name;
    private boolean inconsistent;

    private HashMap<String, String> genreMap = new HashMap<>();

    private void hash_map() {
        genreMap.put("act", "Action");
        genreMap.put("actn", "Action");
        genreMap.put("axtn", "Action");
        genreMap.put("adct", "Adventure");
        genreMap.put("adctx", "Adventure");
        genreMap.put("advt", "Adventure");
        genreMap.put("avant", "Avant Garde");
        genreMap.put("avant garde", "Avant Garde");
        genreMap.put("avga", "Avant Garde");
        genreMap.put("allegory", "Allegory");
        genreMap.put("art video", "Art Video");
        genreMap.put("bio", "Biographical");
        genreMap.put("biop", "Biographical Picture");
        genreMap.put("biopp", "Biographical Picture");
        genreMap.put("camp", "Camp");
        genreMap.put("cart", "Cartoon");
        genreMap.put("cnr", "Cops and Robbers");
        genreMap.put("cnrb", "Cops and Robbers");
        genreMap.put("cond", "Comedy");
        genreMap.put("comd", "Comedy");
        genreMap.put("comd noir", "Black Comedy");
        genreMap.put("crim", "Crime");
        genreMap.put("ctxx", "Uncategorized");
        genreMap.put("cult", "Cult");
        genreMap.put("disa", "Disaster");
        genreMap.put("docu", "Documentary");
        genreMap.put("duco", "Documentary");
        genreMap.put("ducu", "Documentary");
        genreMap.put("docu dram", "Drama Documentary");
        genreMap.put("dram", "Drama");
        genreMap.put("dramd", "Drama");
        genreMap.put("dramn", "Drama");
        genreMap.put("dram>", "Drama");
        genreMap.put("drama", "Drama");
        genreMap.put("draam", "Drama");
        genreMap.put("dram.actn", "Action Drama");
        genreMap.put("dram docu", "Drama Documentary");
        genreMap.put("epic", "Epic");
        genreMap.put("faml", "Family");
        genreMap.put("fant", "Fantasy");
        genreMap.put("hist", "History");
        genreMap.put("hor", "Horror");
        genreMap.put("horr", "Horror");
        genreMap.put("kinky", "Kinky");
        genreMap.put("musical", "Musical");
        genreMap.put("musc", "Musical");
        genreMap.put("myst", "Mystery");
        genreMap.put("mystp", "Mystery");
        genreMap.put("noir", "Black");
        genreMap.put("noir comd", "Black Comedy");
        genreMap.put("noir comd romt", "Black Romantic Comedy");
        genreMap.put("porb", "Pornography");
        genreMap.put("porn", "Pornography");
        genreMap.put("psyc", "Psychological");
        genreMap.put("psych dram", "Psychological Drama");
        genreMap.put("romt", "Romance");
        genreMap.put("romtx", "Romance");
        genreMap.put("romt actn", "Romantic Action");
        genreMap.put("romtadvt", "Romantic Adventure");
        genreMap.put("romt comd", "Romantic Comedy");
        genreMap.put("romt. comd", "Romantic Comedy");
        genreMap.put("romt dram", "Romantic Drama");
        genreMap.put("romt fant", "Romantic Fantasy");
        genreMap.put("scif", "Sci-Fi");
        genreMap.put("scfi", "Sci-Fi");
        genreMap.put("sci-fi", "Sci-Fi");
        genreMap.put("s.f.", "Sci-Fi");
        genreMap.put("sxfi", "Sci-Fi");
        genreMap.put("sports", "Sport");
        genreMap.put("stage musical", "Stage Musical");
        genreMap.put("surl", "Surreal");
        genreMap.put("surr", "Surreal");
        genreMap.put("surreal", "Surreal");
        genreMap.put("susp", "Thriller");
        genreMap.put("tv", "Tv Show");
        genreMap.put("tvser", "Tv Series");
        genreMap.put("tvmini", "Tv Miniseries");
        genreMap.put("viol", "Violence");
        genreMap.put("weird", "Western");
        genreMap.put("west", "Western");
        genreMap.put("west1", "Western");
        genreMap.put("comd west", "Western");
        genreMap.put("verite", "Cinéma Vérité");
    }

    public Genre() {
        hash_map();
    }

    public Genre(Integer id, String name) {
        hash_map();
        this.name = genreMap.getOrDefault(name.toLowerCase().strip(), name);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (genreMap.get(name.toLowerCase().strip()) != null) {
            this.inconsistent = false;
            this.name = genreMap.get(name.toLowerCase().strip());
        } else {
            this.inconsistent = true;
            this.name = name;
        }
    }

    public boolean isInconsistent() {
        return this.inconsistent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(id).append(", ");
        sb.append("Name: ").append(name);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(name)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if ((obj == null) || (!(obj instanceof Genre)))
            return false;

        Genre genre = (Genre) obj;

        return new EqualsBuilder()
                .append(name, genre.name)
                .isEquals();
    }
}
