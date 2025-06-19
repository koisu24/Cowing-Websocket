package Upbit.API.Upbit.websocket;

import Upbit.API.Upbit.entity.Orderbook;
import Upbit.API.Upbit.entity.OrderbookUnit;
import Upbit.API.Upbit.repository.OrderbookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor

public class UpbitOrderbookSocket {

    private final OrderbookRepository orderbookRepository;

    @PostConstruct
    public void connectAll() {
        List<String> markets = List.of("KRW-XRP", "KRW-ETH", "KRW-BTC", "KRW-SNT", "KRW-ALT","KRW-USDT","KRW-SUI", "KRW-SOL",
                "KRW-DOGE","KRW-POKT","KRW-BORA","KRW-RVN","KRW-ORBS","KRW-UNI","KRW-ONDO","KRW-VITUAL","KRW-SOPH","KRW-ABA","KRW-NXPC","KRW-ANIME",
                "KRW-PEPE","KRW-TRUMP","KRW-AGLD","KRW-BCH","KRW-ENS","KRW-SEI","KRW-SHIB","KRW-STMX","KRW-AAVE","KRW-WCT","KRW-STRAX","KRW-LAYER",
                "KRW-TFUEL","KRW-LINK","KRW-MOVE","KRW-TRX","KRW-KAITO","KRW-NEAR","KRW-ARB","KRW-STX","KRW-HBAR","KRW-XLM","KRW-UXLINK","KRW-ZRO","KRW-AVAX","KRW-SAND","KRW-MASK","KRW-T",
                "KRW-DOT","KRW-POL","KRW-AXL","KRW-ME","KRW-MEW","KRW-ETC","KRW-VANA","KRW-LPT","KRW-JTO","KRW-ALGO","KRW-BONK","KRW-DRIFT","KRW-SONIC","KRW-PYTH",
                "KRW-BERA","KRW-A","KRW-TAIKO","KRW-BSV","KRW-BTT","KRW-BLUR","KRW-AERGO","KRW-IMX","KRW-PENDLE","KRW-ICX","KRW-PENGU","KRW-OM","KRW-GRT",
                "KRW-COMP","KRW-ATH","KRW-XEM","KRW-NEO","KRW-INJ","KRW-JUP","KRW-BIGTIME","KRW-APT","KRW-BEAM","KRW-SIGN","KRW-ZETA","KRW-AKT","KRW-CRO","KRW-ARDR","KRW-VET","KRW-GAS",
                "KRW-W","KRW-ATOM","KRW-GMT","KRW-AXS","KRW-TT","KRW-CTC","KRW-FLOW","KRW-AUCTION","KRW-CARV","KRW-MOCA","KRW-MLK","KRW-MAMA","KRW-PUNDIX",
                "KRW-FIL","KRW-IOST","KRW-WAVES","KRW-DEEP","KRW-MNT","KRW-XEC","KRW-RENDER","KRW-CHZ","KRW-SXP","KRW-ORCA","KRW-QTUM","KRW-IOTA","KRW-HIVE","KRW-CVC",
                "KRW-TOKAMAK","KRW-BLAST","KRW-THETA","KRW-HUNT","KRW-MINA","KRW-G","KRW-POLYX","KRW-SAFE","KRW-TIA","KRW-ARKM","KRW-AWE","KRW-WAXP","KRW-STRIKE","KRW-CELO","KRW-ARK",
                "KRW-ID","KRW-ZRX","KRW-HP","KRW-ZIL","KRW-GLM","KRW-GAME2","KRW-CKB","KRW-VTHO","KRW-STG","KRW-COW","KRW-USDC","KRW-EGLD","KRW-MED","KRW-KAVA","KRW-XTZ",
                "KRW-ASTR","KRW-AQT","KRW-IQ","KRW-ONT","KRW-WAL","KRW-MVL","KRW-ELF","KRW-LSK","KRW-ONG","KRW-DKA","KRW-ANKR","KRW-MTL","KRW-JST","KRW-MBL","KRW-BOUNTY",
                "KRW-SC","KRW-QKC","KRW-1INCH","KRW-STEEM","KRW-STORJ","KRW-MOC","KRW-META","KRW-BAT","KRW-POWR","KRW-AHT","KRW-KNC","KRW-CBK","KRW-GRS","KRW-FRCT2");
        int chunkSize = 30;

        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < markets.size(); i += chunkSize) {
            chunks.add(markets.subList(i, Math.min(i + chunkSize, markets.size())));
        }

        for (List<String> chunk : chunks) {
            connect(chunk); // 웹소켓 연결 하나씩 병렬 생성
        }
    }

    private void connect(List<String> marketChunk) {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();

        String codesJsonArray = marketChunk.stream()
                .map(code -> "\"" + code + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        String message = String.format(
                "[{\"ticket\":\"orderbook-only\"},{\"type\":\"orderbook\",\"codes\":[%s]}]",
                codesJsonArray
        );

        client.execute(
                URI.create("wss://api.upbit.com/websocket/v1"),
                session -> session.send(Mono.just(session.textMessage(message)))
                        .thenMany(session.receive()
                                .map(msg -> msg.getPayloadAsText(StandardCharsets.UTF_8))
                                .doOnNext(this::handleMessage)
                        ).then()
        ).subscribe();
    }

    private void handleMessage(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(message);

            String market = json.get("code").asText();

            // Orderbook 조회 or 생성
            Orderbook orderbook = orderbookRepository.findByMarket(market)
                    .orElseGet(() -> {
                        Orderbook newOb = new Orderbook();
                        newOb.setMarket(market);
                        return newOb;
                    });

            // 최신 정보로 업데이트
            orderbook.setTimestamp(json.get("timestamp").asLong());
            orderbook.setTotalAskSize(json.get("total_ask_size").asDouble());
            orderbook.setTotalBidSize(json.get("total_bid_size").asDouble());
            orderbook.setLevel(json.get("level").asDouble());


            // 새로운 unit 목록 생성
            List<OrderbookUnit> units = new ArrayList<>();
            for (JsonNode unit : json.get("orderbook_units")) {
                OrderbookUnit ou = new OrderbookUnit();
                ou.setOrderbook(orderbook);
                ou.setAskPrice(unit.get("ask_price").asDouble());
                ou.setBidPrice(unit.get("bid_price").asDouble());
                ou.setAskSize(unit.get("ask_size").asDouble());
                ou.setBidSize(unit.get("bid_size").asDouble());
                units.add(ou);
            }

            orderbook.setUnits(units);
            orderbookRepository.save(orderbook);

        } catch (Exception e) {
            log.error("Error parsing orderbook message", e);
        }
    }
}

