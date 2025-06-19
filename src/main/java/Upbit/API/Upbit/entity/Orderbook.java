package Upbit.API.Upbit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// Orderbook.java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orderbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String market;
    private Long timestamp;
    private Double totalAskSize;
    private Double totalBidSize;
    private Double level;

    @OneToMany(mappedBy = "orderbook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderbookUnit> units = new ArrayList<>();
}
