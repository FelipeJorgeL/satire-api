package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.internal.model.Address;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findAllByCustomerIdOrderByPrimaryDescCreatedAtAsc(UUID customerId);

    Optional<Address> findByIdAndCustomerId(UUID id, UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select address
        from Address address
        where address.id = :id and address.customerId = :customerId
        """)
    Optional<Address> findByIdAndCustomerIdForUpdate(
        @Param("id") UUID id,
        @Param("customerId") UUID customerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select address
        from Address address
        where address.customerId = :customerId
        order by address.primary desc, address.createdAt asc
        """)
    List<Address> findAllByCustomerIdForUpdate(@Param("customerId") UUID customerId);
}
