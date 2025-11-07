package upc.edu.ecomovil.api.iam.application.internal.eventhandlers;

import upc.edu.ecomovil.api.iam.domain.model.commands.SeedRolesCommand;
import upc.edu.ecomovil.api.iam.domain.services.RoleCommandService;
import upc.edu.ecomovil.api.plan2.domain.model.commands.CreatePlan2Command;
import upc.edu.ecomovil.api.plan2.domain.model.queries.GetPlan2ByIdQuery;
import upc.edu.ecomovil.api.plan2.domain.services.Plan2CommandService;
import upc.edu.ecomovil.api.plan2.domain.services.Plan2QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * ApplicationReadyEventHandler class
 * This class is used to handle the ApplicationReadyEvent
 */
@Service
public class ApplicationReadyEventHandler {
    private final RoleCommandService roleCommandService;
    private final Plan2CommandService plan2CommandService;
    private final Plan2QueryService plan2QueryService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService, Plan2CommandService plan2CommandService, Plan2QueryService plan2QueryService) {
        this.roleCommandService = roleCommandService;
        this.plan2CommandService = plan2CommandService;
        this.plan2QueryService = plan2QueryService;
    }

    /**
     * Handle the ApplicationReadyEvent
     * This method is used to seed the roles
     * @param event the ApplicationReadyEvent the event to handle
     */
    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();

        // --- Lógica de Roles (se mantiene) ---
        LOGGER.info("Starting to verify if roles seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedRolesCommand = new SeedRolesCommand();
        roleCommandService.handle(seedRolesCommand);
        LOGGER.info("Roles seeding verification finished for {} at {}", applicationName, currentTimestamp());

        // --- Nueva Lógica de Planes ---
        LOGGER.info("Starting to verify if default plan seeding is needed for {} at {}", applicationName, currentTimestamp());

        // 3. Verificamos si el plan con ID 1 ya existe
        var getPlanByIdQuery = new GetPlan2ByIdQuery(1L);
        var defaultPlan = plan2QueryService.handle(getPlanByIdQuery);

        // 4. Si no existe (el resultado está vacío), lo creamos
        if (defaultPlan.isEmpty()) {
            LOGGER.info("Default plan (ID 1) not found. Seeding default plan...");

            // 👇 **INICIO DE LA CORRECCIÓN**
            // Cambiamos 0L (Long) por 0.0 (Double) para que coincida con el constructor
            var createPlanCommand = new CreatePlan2Command("Plan Básico", "Plan por defecto para nuevos usuarios", 0.0);
            // 👆 **FIN DE LA CORRECCIÓN**

            plan2CommandService.handle(createPlanCommand);
            LOGGER.info("Default plan seeding finished.");
        } else {
            LOGGER.info("Default plan (ID 1) already exists. No seeding needed.");
        }
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}