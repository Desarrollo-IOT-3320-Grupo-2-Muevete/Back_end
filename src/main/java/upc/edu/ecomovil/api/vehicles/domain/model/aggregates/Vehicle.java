package upc.edu.ecomovil.api.vehicles.domain.model.aggregates;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter; // 👈 IMPORTANTE: Añadimos esto
import upc.edu.ecomovil.api.plan2.domain.model.aggregates.Plan2;
import upc.edu.ecomovil.api.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import upc.edu.ecomovil.api.user.domain.model.aggregates.Profile;
import upc.edu.ecomovil.api.vehicles.domain.model.commands.CreateVehicleCommand;
import upc.edu.ecomovil.api.vehicles.domain.model.valueobjects.Details;
import upc.edu.ecomovil.api.vehicles.domain.model.valueobjects.Prices;
import upc.edu.ecomovil.api.vehicles.domain.model.valueobjects.Review;

@Entity
public class Vehicle extends AuditableAbstractAggregateRoot<Vehicle> {

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false) // FK a Profile
    @Getter
    private Profile owner;


    @Embedded
    private Details details;

    @Embedded
    private Review review;

    @Embedded
    private Prices prices;

    @Getter
    private Boolean isAvailable;

    @Getter
    private String ImageUrl;

    @Getter
    @Setter // 👈 Añadido para que el simulador pueda actualizar la latitud
    private Float lat; //latitud

    @Getter
    @Setter // 👈 Añadido para que el simulador pueda actualizar la longitud
    private Float lng; //longitud

    @Getter
    private String description;

    public Vehicle(String type, String name, Integer year, Integer review, Double pricerent, Double pricesell, Boolean isAvailable, String imageUrl, Float lat, Float lng, String description) {
        this.details = new Details(type, name, year);
        this.review = new Review(review);
        this.prices = new Prices(pricerent, pricesell);
        this.isAvailable = isAvailable;
        this.ImageUrl = imageUrl;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
    }

    public Vehicle(CreateVehicleCommand command, Profile owner) {
        this.details = new Details(command.type(), command.name(), command.year());
        this.review = new Review(command.review());
        this.prices = new Prices(command.pricerent(), command.pricesell());
        this.isAvailable = command.isAvailable();
        this.ImageUrl = command.imageUrl();
        this.lat = command.lat();
        this.lng = command.lng();
        this.description = command.description();
        this.owner = owner;
    }

    public Vehicle(){}

    public void updateDetails(String type, String name, Integer year){
        this.details = new Details(type, name, year);
    }

    public void updateReview(Integer review){
        this.review = new Review(review);
    }

    public void updatePrices(Double pricerent, Double pricesell){
        this.prices = new Prices(pricerent, pricesell);
    }

    public void updateIsAvailable(Boolean isAvailable){
        this.isAvailable = isAvailable;
    }

    public void updateImageUrl(String imageUrl){
        this.ImageUrl = imageUrl;
    }

    // Ya no necesitas estos métodos manuales updateLat/updateLng si usas @Setter,
    // pero los dejamos por si acaso los usas en otro lado.
    public void updateLat(Float lat){
        this.lat = lat;
    }

    public void updateLng(Float lng){
        this.lng = lng;
    }

    public String getType(){
        return details.getType();
    }

    public String getName(){
        return details.getName();
    }

    public Integer getYear(){
        return details.getYear();
    }

    public Integer getReview(){
        return review.getReview();
    }

    public Double getPriceRent(){
        return prices.getPriceRent();
    }

    public Double getPriceSell(){
        return prices.getPriceSell();
    }

}