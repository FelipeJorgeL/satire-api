package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.internal.dto.request.AdminCustomerFilter;
import br.com.api.satireapi.domain.customer.internal.model.Address;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.domain.customer.internal.usecase.administration.AdminCustomerQuery;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
class JpaAdminCustomerQuery implements AdminCustomerQuery {

    private final CustomerRepository customerRepository;
    private final ProfileRepository profileRepository;
    private final AddressRepository addressRepository;

    JpaAdminCustomerQuery(
        CustomerRepository customerRepository,
        ProfileRepository profileRepository,
        AddressRepository addressRepository
    ) {
        this.customerRepository = customerRepository;
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public Page<Customer> findAll(AdminCustomerFilter filter, Pageable pageable) {
        Specification<Customer> specification = (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (filter.search() != null) {
                var pattern = "%" + escapeLike(filter.search()) + "%";
                predicates.add(builder.or(
                    builder.like(builder.lower(root.get("name")), pattern, '\\'),
                    builder.like(builder.lower(root.get("email")), pattern, '\\')
                ));
            }
            if (filter.active() != null) {
                predicates.add(builder.equal(root.get("active"), filter.active()));
            }
            if (filter.profile() != null) {
                var profile = root.join("profiles");
                predicates.add(builder.equal(profile.get("name"), filter.profile()));
                query.distinct(true);
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return customerRepository.findAll(specification, pageable);
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        return customerRepository.findById(customerId);
    }

    @Override
    public Optional<Customer> findByIdForUpdate(UUID customerId) {
        return customerRepository.findByIdForUpdate(customerId);
    }

    @Override
    public List<Address> findAddressesByCustomerId(UUID customerId) {
        return addressRepository.findAllByCustomerIdOrderByPrimaryDescCreatedAtAsc(customerId);
    }

    @Override
    public Optional<Profile> findProfileByName(String profileName) {
        return profileRepository.findByName(profileName);
    }

    @Override
    public Optional<Profile> findProfileByNameForUpdate(String profileName) {
        return profileRepository.findByNameForUpdate(profileName);
    }

    @Override
    public long countByProfileName(String profileName) {
        return customerRepository.countByProfileName(profileName);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
