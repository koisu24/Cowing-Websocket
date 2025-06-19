package Upbit.API.Upbit.repository;


import Upbit.API.Upbit.entity.Orderbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderbookRepository extends JpaRepository<Orderbook, Long> {
    Optional<Orderbook> findByMarket(String market);

}