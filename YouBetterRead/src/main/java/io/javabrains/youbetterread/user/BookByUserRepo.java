package io.javabrains.youbetterread.user;

import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Slice;

public interface BookByUserRepo extends CassandraRepository<BooksbyUser, String> {
    Slice<BooksbyUser> findAllById(String id, CassandraPageRequest cassandraPageRequest);

}
