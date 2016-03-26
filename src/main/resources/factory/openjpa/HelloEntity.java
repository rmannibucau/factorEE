package {{package}}.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@NamedQueries({
        @NamedQuery(name = "HelloEntity.findAll", query = "select e from HelloEntity e order by u.name"),
        @NamedQuery(name = "HelloEntity.findByName", query = "select e from HelloEntity e where e.name = :name")
})
@Table(name = "hello")
public class HelloEntity {
    @Id
    @GeneratedValue
    private long id;

    @Version
    private long version;

    @Column(length = 160)
    private String name;

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(final long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
