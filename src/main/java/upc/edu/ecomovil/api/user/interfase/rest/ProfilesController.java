package upc.edu.ecomovil.api.user.interfase.rest;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import upc.edu.ecomovil.api.iam.domain.model.aggregates.User;
import upc.edu.ecomovil.api.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import upc.edu.ecomovil.api.user.domain.model.aggregates.Profile;
import upc.edu.ecomovil.api.user.domain.model.queries.GetAllProfilesQuery;
import upc.edu.ecomovil.api.user.domain.model.queries.GetProfileByIdQuery;
import upc.edu.ecomovil.api.user.domain.services.ProfileCommandService;
import upc.edu.ecomovil.api.user.domain.services.ProfileQueryService;
import upc.edu.ecomovil.api.user.infrastructure.persistence.jpa.repositories.ProfileRepository;
import upc.edu.ecomovil.api.user.interfase.rest.resources.CreateProfileResource;
import upc.edu.ecomovil.api.user.interfase.rest.resources.ProfileResource;
import upc.edu.ecomovil.api.user.interfase.rest.transform.CreateProfileCommandFromResourceAssembler;
import upc.edu.ecomovil.api.user.interfase.rest.transform.ProfileResourceFromEntityAssembler;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile Management Endpoints")
public class ProfilesController {
    private final ProfileQueryService profileQueryService;
    private final ProfileCommandService profileCommandService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;


    public ProfilesController(ProfileQueryService profileQueryService, ProfileCommandService profileCommandService, UserRepository userRepository, ProfileRepository profileRepository) {
        this.profileQueryService = profileQueryService;
        this.profileCommandService = profileCommandService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }


    @PostMapping
    public ResponseEntity<ProfileResource> createProfile(@RequestBody CreateProfileResource resource,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Authenticated as: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var createProfileCommand = CreateProfileCommandFromResourceAssembler.toCommandFromResource(resource, user);
        var profile = profileCommandService.handle(createProfileCommand);

        if (profile.isEmpty()) return ResponseEntity.badRequest().build();

        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return new ResponseEntity<>(profileResource, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get a profile by ID",
            description = "Gets a profile by the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })

    @GetMapping("/me")
    public ResponseEntity<ProfileResource> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        var profileOptional = profileRepository.findById(user.getId());
        if (profileOptional.isEmpty()) return ResponseEntity.notFound().build();

        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profileOptional.get());
        return ResponseEntity.ok(profileResource);
    }


    @GetMapping
    public ResponseEntity<List<ProfileResource>> getAllProfiles() {
        var getAllProfilesQuery = new GetAllProfilesQuery();
        var profiles = profileQueryService.handle(getAllProfilesQuery);
        var profileResources = profiles.stream().map(ProfileResourceFromEntityAssembler::toResourceFromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(profileResources);
    }

    @PutMapping
    public ResponseEntity<ProfileResource> updateProfile(@RequestBody CreateProfileResource resource) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        var profileOptional = profileRepository.findById(user.getId());
        if (profileOptional.isEmpty()) return ResponseEntity.notFound().build();

        Profile profile = profileOptional.get();


        if (resource.firstName() != null && !resource.firstName().isBlank() &&
                resource.lastName() != null && !resource.lastName().isBlank()) {
            profile.updateName(resource.firstName(), resource.lastName());
        }

        if (resource.email() != null && !resource.email().isBlank()) {
            profile.updateEmail(resource.email());
        }

        if (resource.phoneNumber() != null && !resource.phoneNumber().isBlank()) {
            profile.updatePhoneNumber(resource.phoneNumber());
        }

        if (resource.ruc() != null && !resource.ruc().isBlank()) {
            profile.updateRuc(resource.ruc());
        }

        var savedProfile = profileRepository.save(profile);
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(savedProfile);

        return ResponseEntity.ok(profileResource);
    }
}