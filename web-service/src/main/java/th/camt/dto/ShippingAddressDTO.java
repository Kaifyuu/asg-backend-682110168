package th.camt.dto;

public class ShippingAddressDTO {

    private Long id;
    private String address;
    private String city;
    private String postalCode;
    private String country;

    public ShippingAddressDTO() {
    }

    public ShippingAddressDTO(Long id, String address, String city, String postalCode, String country) {
        this.id = id;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
