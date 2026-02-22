package it.matteoroxis.mongo_rbac_engine.repository;
import it.matteoroxis.mongo_rbac_engine.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface OrderRepository extends MongoRepository<OrderDocument, String> {
    List<OrderDocument> findByUserId(String userId);
}