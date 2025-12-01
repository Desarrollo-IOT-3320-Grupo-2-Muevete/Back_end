package upc.edu.ecomovil.api.vehicles.interfase.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.ecomovil.api.vehicles.domain.model.aggregates.Vehicle;
import upc.edu.ecomovil.api.vehicles.infraestructure.persistence.jpa.repositories.VehicleRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/iot")
@Tag(name = "IoT", description = "Endpoints para dispositivos Edge (Hardware)")
public class IotController {

    private final VehicleRepository vehicleRepository;

    public IotController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * El dispositivo llama aquí para decir: "ESTOY AQUÍ" (Enviar Datos)
     * Recibe: { "lat": -12.555, "lng": -77.555, "battery": 80 }
     */
    @PostMapping("/vehicle/{vehicleId}/telemetry")
    public ResponseEntity<?> updateTelemetry(@PathVariable Long vehicleId, @RequestBody Map<String, Object> payload) {

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);

        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();

            // Extraemos los datos que manda el hardware
            if (payload.containsKey("lat") && payload.containsKey("lng")) {
                // Convertimos a float (asegurando que no falle si viene como string o double)
                float lat = Float.parseFloat(payload.get("lat").toString());
                float lng = Float.parseFloat(payload.get("lng").toString());

                vehicle.setLat(lat);
                vehicle.setLng(lng);

                // Aquí podrías guardar también la batería si tuvieras el campo en la BD
                // System.out.println("Batería: " + payload.get("battery"));

                vehicleRepository.save(vehicle);
                return ResponseEntity.ok("Ubicación actualizada");
            }
            return ResponseEntity.badRequest().body("Faltan coordenadas (lat, lng)");
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * El dispositivo llama aquí para preguntar: "¿QUÉ HAGO?" (Recibir Datos/Comandos)
     * Ejemplo: Saber si debe bloquearse o desbloquearse.
     */
    @GetMapping("/vehicle/{vehicleId}/status")
    public ResponseEntity<?> getVehicleStatus(@PathVariable Long vehicleId) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);

        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            // Retornamos un JSON simple que el hardware pueda leer fácil
            // Ejemplo: { "locked": false, "maxSpeed": 25 }
            return ResponseEntity.ok(Map.of(
                    "isAvailable", vehicle.getIsAvailable(), // El hardware puede bloquearse si no está disponible
                    "id", vehicle.getId()
            ));
        }
        return ResponseEntity.notFound().build();
    }
}