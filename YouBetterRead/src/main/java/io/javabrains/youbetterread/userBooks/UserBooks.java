package io.javabrains.youbetterread.userBooks;

import java.time.LocalDate;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

@Table(value = "book_by_userid_and_bookid")
public class UserBooks {

    @PrimaryKey
    private UserBookprimaryKey key;

    @Column("reading_status")
    @CassandraType(type = Name.TEXT)
    private String readingStatus;

    @Column("start_date")
    @CassandraType(type = Name.DATE)
    private LocalDate startDate;

    @Column("end_date")
    @CassandraType(type = Name.DATE)
    private LocalDate endDate;

    @Column("rating")
    @CassandraType(type = Name.INT)
    private int rating;

    public UserBookprimaryKey getKey() {
        return key;
    }

    public void setKey(UserBookprimaryKey key) {
        this.key = key;
    }

    public String getReadingStatus() {
        return readingStatus;
    }

    public void setReadingStatus(String readingStatus) {
        this.readingStatus = readingStatus;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

}
