package swst.application.models;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import swst.application.relationkeyclasses.ProductsColorRelationKey;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "productscolor")
public class ProductsColor {

	@EmbeddedId
	private ProductsColorRelationKey id;

	private String imageCase;

	@ManyToOne
	@MapsId("caseID")
	@JoinColumn(name = "caseID", referencedColumnName = "caseID")
	private Products caseID;

	@ManyToOne
	@MapsId("codeColor")
	@JoinColumn(name = "codeColor" , referencedColumnName = "codeColor")
	private Colors codeColor;
}
