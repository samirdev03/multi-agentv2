package org.example.session;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface MessageTurnRepository extends JpaRepository<MessageTurnEntity,Long>{Optional<MessageTurnEntity> findByRequestId(UUID id); @Modifying @Query("update MessageTurnEntity t set t.status=:processing where t.requestId=:id and t.status=:pending") int claim(@Param("id")UUID id,@Param("processing")MessageTurnStatus processing,@Param("pending")MessageTurnStatus pending);}
