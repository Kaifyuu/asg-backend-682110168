package th.mfu.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A customer of the shop.
 *
 * Relationships:
 *  - One-to-One with {@link ShippingAddress} (each customer has exactly one shipping address)
 *  - One-to-Many with {@link Order} (a customer can place many orders)
 */
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "displayname")
    private String displayname;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birthday")
    private LocalDate birthday;

    // Inverse (non-owning) side of the One-to-One relationship: ShippingAddress
    // owns the foreign key (customer_id), so this side just points to it via
    // mappedBy. cascade=ALL + orphanRemoval means saving/deleting a Customer
    // automatically saves/deletes its address too - no need to manage it separately.
    // @JsonIgnore prevents Jackson from serializing the entity directly (we return
    // DTOs from the controller instead), which also avoids infinite recursion since
    // ShippingAddress holds a back-reference to Customer.
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private ShippingAddress shippingAddress;

    // Inverse side of the One-to-Many relationship: Order owns the FK (customer_id).
    // Same cascade/orphanRemoval reasoning as above - deleting a Customer deletes
    // all of their Orders (and, transitively, each Order's OrderItems).
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    public Customer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    /** Keeps both sides of the one-to-one association in sync. */
    public void setShippingAddress(ShippingAddress shippingAddress) {
        if (shippingAddress == null) {
            if (this.shippingAddress != null) {
                this.shippingAddress.setCustomer(null);
            }
        } else {
            shippingAddress.setCustomer(this);
        }
        this.shippingAddress = shippingAddress;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }
}
