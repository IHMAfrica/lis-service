package zm.gov.moh.lisservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import zm.gov.moh.lisservice.config.ConfigProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final ConfigProperties configProperties;

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");

        List<String> keycloakClientIds = List.of(configProperties.getKeycloakClientId());

        for (String clientId: keycloakClientIds) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);

            if (clientAccess != null) {
                Collection<String> roles = (Collection<String>) clientAccess.get("roles");

                authorities.addAll(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                .toList());
            }
        }

        return authorities;
    }
}
