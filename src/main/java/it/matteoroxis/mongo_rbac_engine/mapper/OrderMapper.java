package it.matteoroxis.mongo_rbac_engine.mapper;
import it.matteoroxis.mongo_rbac_engine.document.OrderDocument;
import it.matteoroxis.mongo_rbac_engine.dto.request.OrderRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.OrderResponse;
import org.springframework.stereotype.Component;
import java.time.Instant;
@Component
public class OrderMapper {
    public OrderDocument toDocument(OrderRequest request) {
        OrderDocument doc = new OrderDocument();
        doc.setUserId(request.getUserId());
        doc.setDescription(request.getDescription());
        doc.setStatus("PENDING");
        doc.setCreatedAt(Instant.now());
        return doc;
    }
    public OrderResponse toResponse(OrderDocument doc) {
        return new OrderResponse(
                doc.getId(),
                doc.getUserId(),
                doc.getDescription(),
                doc.getStatus(),
                doc.getCreatedAt()
        );
    }
}