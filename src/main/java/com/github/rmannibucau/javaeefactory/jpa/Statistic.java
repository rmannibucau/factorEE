package com.github.rmannibucau.javaeefactory.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.Version;
import java.util.Date;

import static javax.persistence.TemporalType.DATE;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@Entity
public class Statistic {
    @EmbeddedId
    private TemporalId id;
    private int total;

    @Version
    private int version;

    public void increment() {
        total++;
    }

    @Data
    @Embeddable
    public static class TemporalId {
        private String name;

        @Temporal(DATE)
        private Date date;
    }
}
