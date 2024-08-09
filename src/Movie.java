import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Movie {
    private String id;
    private String xmlId;
    private String title;
    private int year;
    private String director;

    List<Genre> genres;
    List<Star> stars;

    public Movie() { genres = new ArrayList<>(); stars = new ArrayList<>(); }

    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public String getId() {
        return id;
    }

    public String getxmlId() {
        return xmlId;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setxmlId(String id) {
        this.xmlId = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String printGenres() {
        StringBuilder sb = new StringBuilder();

        Iterator<Genre> it = genres.iterator();
        sb.append('[');
        while (it.hasNext()) {
            sb.append('"');
            sb.append(it.next());
            sb.append('"');
            if (it.hasNext()) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void addGenre(Genre genre) {
        this.genres.add(genre);
    }

    public String printStars() {
        StringBuilder sb = new StringBuilder();

        Iterator<Star> it = stars.iterator();
        sb.append('[');
        while (it.hasNext()) {
            sb.append('"');
            sb.append(it.next());
            sb.append('"');
            if (it.hasNext()) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public List<Star> getStars() {
        return stars;
    }

    public void addStars(Star star) {
        this.stars.add(star);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Id:" + getId());
        sb.append(", ");
        sb.append("Title:" + getTitle());
        sb.append(", ");
        sb.append("Year:" + getYear());
        sb.append(", ");
        sb.append("Director:" + getDirector());
        sb.append(", ");
        sb.append("Genres:" + printGenres());
        sb.append(", ");
        sb.append("Stars:" + printStars());

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31) // two randomly chosen prime numbers
                .append(title)
                .append(year)
                .append(director)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if ((obj == null) || (!(obj instanceof Movie)))
            return false;

        Movie movie = (Movie) obj;

        return new EqualsBuilder()
                .append(title, movie.title)
                .append(year, movie.year)
                .append(director, movie.director)
                .isEquals();
    }
}
