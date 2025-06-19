package Upbit.API.Upbit.repository;

import Upbit.API.Upbit.entity.OrderbookUnit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderbootUnitRepository {
    public interface OrderbookUnitRepository extends JpaRepository<OrderbookUnit, Long> {}
}
