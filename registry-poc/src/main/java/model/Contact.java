package model;

import java.util.Objects;

/**
 * Contact entity representing contact information for domain registrations.
 * 
 * Simplified version inspired by Nomulus's Contact model.
 */
public final class Contact {
    
    public enum Type {
        REGISTRANT,
        ADMIN,
        TECH,
        BILLING
    }
    
    private final String contactId;
    private final String name;
    private final String organization;
    private final String email;
    private final String phone;
    private final Address address;
    private final Type type;
    
    private Contact(Builder builder) {
        this.contactId = builder.contactId;
        this.name = builder.name;
        this.organization = builder.organization;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.type = builder.type;
    }
    
    public String getContactId() {
        return contactId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public Type getType() {
        return type;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(contactId, contact.contactId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(contactId);
    }
    
    @Override
    public String toString() {
        return "Contact{" +
                "contactId='" + contactId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                '}';
    }
    
    public static final class Builder {
        private String contactId;
        private String name;
        private String organization;
        private String email;
        private String phone;
        private Address address;
        private Type type = Type.REGISTRANT;
        
        public Builder setContactId(String contactId) {
            this.contactId = contactId;
            return this;
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder setOrganization(String organization) {
            this.organization = organization;
            return this;
        }
        
        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }
        
        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder setAddress(Address address) {
            this.address = address;
            return this;
        }
        
        public Builder setType(Type type) {
            this.type = type;
            return this;
        }
        
        public Contact build() {
            Objects.requireNonNull(contactId, "Contact ID is required");
            Objects.requireNonNull(name, "Contact name is required");
            Objects.requireNonNull(email, "Contact email is required");
            
            return new Contact(this);
        }
    }
    
    /**
     * Value object for address information
     */
    public static final class Address {
        private final String street;
        private final String city;
        private final String state;
        private final String postalCode;
        private final String country;
        
        public Address(String street, String city, String state, String postalCode, String country) {
            this.street = street;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = Objects.requireNonNull(country, "Country is required");
        }
        
        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getState() { return state; }
        public String getPostalCode() { return postalCode; }
        public String getCountry() { return country; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Address address = (Address) o;
            return Objects.equals(street, address.street) &&
                   Objects.equals(city, address.city) &&
                   Objects.equals(state, address.state) &&
                   Objects.equals(postalCode, address.postalCode) &&
                   Objects.equals(country, address.country);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(street, city, state, postalCode, country);
        }
    }
}