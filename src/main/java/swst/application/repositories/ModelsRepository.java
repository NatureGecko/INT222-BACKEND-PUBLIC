package swst.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import swst.application.model_products.Models;

public interface ModelsRepository extends JpaRepository<Models, Integer>{

}