package th.mfu.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A line item within a {@link Order}, referencing a {@link Product}.
 *
 * Relationships:
 *  - Many-to-One with {@link Order} (many items belong to one order)
 *  - Many-to-One with {@link Product} (many items can reference one product)
 */
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity")
    private Integer quantity;

    // Snapshot of Product.price at the moment the order was placed (copied in
    // OrderService.create()). Deliberately denormalized: if the product's price
    // changes later, past orders should still show what the customer actually paid.
    @Column(name = "unit_price")
    private Double unitPrice;

    // Owning side of Many-to-One with Order: this class holds the FK (order_id).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    // Owning side of Many-to-One with Product: this class holds the FK
    // (product_id). No @JsonIgnore here since OrderItem itself is never
    // serialized directly - only via OrderItemDTO, which reads productId/
    // productName out of this association.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
