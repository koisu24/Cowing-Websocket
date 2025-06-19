package Upbit.API.Upbit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orderbook_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderbookUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double askPrice;
    private Double bidPrice;
    private Double askSize;
    private Double bidSize;
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderbook_id")
    private Orderbook orderbook;
}