package swst.application.models_orders;

import javax.persistence.*;
import swst.application.relationkeyclasses.OrderDetailRelationKey;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "orderdetail")
public class OrderDetail {
	
	@EmbeddedId
	private OrderDetailRelationKey id;
	
	private int quantityOrder;
	private float unitPrice;
	
	//@ManyToOne
	//@JoinColumn(name = "caseID", referencedColumnName = "caseID")  
	//private Products product;
	
}
