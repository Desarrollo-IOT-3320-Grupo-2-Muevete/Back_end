package upc.edu.ecomovil.api.vehicles.application.internal.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import upc.edu.ecomovil.api.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.api.vehicles.infraestructure.persistence.jpa.repositories.VehicleRepository;

import java.util.List;

@Service
public class VehicleSimulatorService {

    private final VehicleRepository vehicleRepository;

    public VehicleSimulatorService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // Se ejecuta automáticamente cada 5 segundos (5000 milisegundos)
    @Scheduled(fixedRate = 5000)
    public void simulateMovement() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        if (vehicles.isEmpty()) {
            return;
        }

        for (Vehicle v : vehicles) {
            // Solo movemos los vehículos si tienen coordenadas iniciales
            if (v.getLat() != null && v.getLng() != null) {

                // Simular movimiento: Sumar/Restar un número muy pequeño (aprox 10 metros)
                // Math.random() genera entre 0.0 y 1.0
                float movementFactor = 0.0001f;

                float newLat = v.getLat() + (float) (Math.random() * (movementFactor * 2) - movementFactor);
                float newLng = v.getLng() + (float) (Math.random() * (movementFactor * 2) - movementFactor);

                v.setLat(newLat);
                v.setLng(newLng);

                vehicleRepository.save(v);
            }
        }
        System.out.println("🚜 Simulación IoT: Coordenadas de vehículos actualizadas.");
    }
}