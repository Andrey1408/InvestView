package ivbackend.tinkoff.service

import ivbackend.db.dao.ShareDAO
import ivbackend.tinkoff.model.APIshare
import io.ktor.features.*
import io.smallrye.mutiny.Multi
import org.reactivestreams.FlowAdapters
import org.slf4j.LoggerFactory
import ru.tinkoff.piapi.contract.v1.CandleInstrument
import ru.tinkoff.piapi.contract.v1.MarketDataRequest
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.contract.v1.Quotation
import ru.tinkoff.piapi.contract.v1.SubscribeCandlesRequest
import ru.tinkoff.piapi.contract.v1.SubscriptionAction
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.core.InvestApi
import java.math.BigDecimal
import java.util.concurrent.SubmissionPublisher
class InvestApiService(
    private val shareDao: ShareDAO,
    private val api: InvestApi
) {
    private val log = LoggerFactory.getLogger(InvestApiService::class.java)
    private val marketDataRequestPublisher = SubmissionPublisher<MarketDataRequest>()

    init {
        Multi.createFrom()
            .safePublisher(FlowAdapters.toPublisher(api.marketDataService.marketDataStream(marketDataRequestPublisher)))
            .subscribe()
            .with(this::onMarketDataResponse)
    }

    fun getShare(figi: String): APIshare {
        val investShare = api.instrumentsService.getShareByFigiSync(figi)
            .orElseThrow { NotFoundException("Share info not found by figi: $figi") }

        return APIshare(investShare.name, investShare.currency)
    }

    fun getSharePrice(figi: String): BigDecimal {
        val lastPrice = api.marketDataService.getLastPricesSync(listOf(figi))
            .find { it.figi == figi } ?: throw IllegalStateException("Can't get last price for figi=$figi")
        return lastPrice.price.toBigDecimal()
    }

    fun subscribeOnPriceUpdate(figi: String) {
        val subscribeRequest = marketDataSubscribeCandlesRequest(listOf(figi))
        log.info("Subscribing on {}", figi)
        marketDataRequestPublisher.submit(subscribeRequest)
    }

    fun subscribeOnAddedFigiPriceUpdate() {
        val sharesFigi = shareDao.findAll().map { it.figi }
        val subscribeRequest = marketDataSubscribeCandlesRequest(sharesFigi)
        log.info("Subscribing on {}", sharesFigi)
        marketDataRequestPublisher.submit(subscribeRequest)
    }

    private fun marketDataSubscribeCandlesRequest(figiList: List<String>): MarketDataRequest {
        val builder = SubscribeCandlesRequest.newBuilder()
            .setSubscriptionAction(SubscriptionAction.SUBSCRIPTION_ACTION_SUBSCRIBE)
        for (figi in figiList) {
            builder.addInstruments(
                CandleInstrument.newBuilder()
                    .setFigi(figi)
                    .setInterval(SubscriptionInterval.SUBSCRIPTION_INTERVAL_FIVE_MINUTES)
                    .build()
            )
        }
        return MarketDataRequest.newBuilder()
            .setSubscribeCandlesRequest(builder.build())
            .build()
    }

    private fun onMarketDataResponse(response: MarketDataResponse) {
        if (response.hasCandle()) {
            val candle = response.candle
            val figi = candle.figi
            val candleClosePrice = candle.close.toBigDecimal()
            shareDao.updatePrice(figi, candleClosePrice)
            log.info("Updated $figi price to $candleClosePrice")
        } else if (response.hasSubscribeCandlesResponse()) {
            log.info("Subscribe candles response: {}", response.subscribeCandlesResponse)
        }
    }


    private fun Quotation.toBigDecimal(): BigDecimal {
        return if (units == 0L && nano == 0) {
            BigDecimal.ZERO
        } else {
            BigDecimal.valueOf(units).add(BigDecimal.valueOf(nano.toLong(), 9))
        }
    }
}