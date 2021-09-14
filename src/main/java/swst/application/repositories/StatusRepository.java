package swst.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import swst.application.models_orders.OrderStatus;

public interface StatusRepository extends JpaRepository<OrderStatus, Integer>{

}