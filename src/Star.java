import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Star {
    private String id;
    private String xmlId;
    private String name;
    private int birthYear;
    private String director;

    String movie;

    public Star() {
    }

    public Star(String id, String name, int birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getxmlId() {
        return xmlId;
    }

    public void setxmlId(String xmlId) {
        this.xmlId = xmlId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setYear(int year) {
        this.birthYear = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getMovie() {
        return movie;
    }

    public void addMovie(String m) { this.movie = m; }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("ID:" + getId());
        sb.append(", ");
        sb.append("Name:" + getName());
        sb.append(", ");
        sb.append("Year:" + getBirthYear());
        sb.append(", ");
        sb.append("Movies:" + getMovie());
        sb.append(".");

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                append(name).
                append(birthYear).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Star))
            return false;
        if ((obj == null) || (!(obj instanceof Genre)))
            return false;

        Star star = (Star) obj;
        return new EqualsBuilder()
                .append(name, star.name)
                .append(birthYear, star.birthYear)
                .isEquals();
    }
}