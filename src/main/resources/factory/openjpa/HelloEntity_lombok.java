package {{package}}.jpa;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
@Getter @Setter
public class HelloEntity {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private long id;

    @Version
    private long version;

    @Column(length = 160)
    private String name;
}
