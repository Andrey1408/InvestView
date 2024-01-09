package ivbackend.tinkoff.service

import ivbackend.db.dao.ShareDAO
import ivbackend.tinkoff.model.Share
import io.ktor.features.*

class ShareService(
    private val shareDao: ShareDAO,
    private val investApiService: InvestApiService,
) {

    fun getShares(): List<Share> {
        return shareDao.findAll()
    }

    fun getShareOrThrow(figi: String): Share {
        return shareDao.find(figi) ?: throw NotFoundException("Figi $figi not found")
    }

    fun addShareToExchange(figi: String, availableQuantity: Long): Share {
        if (shareDao.find(figi) != null) {
            throw BadRequestException("Share $figi already added to exchange")
        }

        val investShare = investApiService.getShare(figi)
        if (investShare.currency.uppercase() != RUB_CURRENCY_ISO) {
            throw BadRequestException("Share {figi=$figi, name=${investShare.name}} is not tradable in rubles")
        }

        val price = investApiService.getSharePrice(figi)
        val share = Share(figi, investShare.name, price, availableQuantity)

        shareDao.insert(share.figi, share.name, share.price, share.available)
        investApiService.subscribeOnPriceUpdate(figi)

        return share
    }

    fun increaseAvailableShares(figi: String, quantity: Long) {
        getShareOrThrow(figi)
        shareDao.addAvailableQuantity(figi, quantity)
    }

    fun increaseAvailableShares(share: Share, quantity: Long) {
        shareDao.addAvailableQuantity(share.figi, quantity)
    }

    fun decreaseAvailableShares(share: Share, quantity: Long) {
        shareDao.addAvailableQuantity(share.figi, -quantity)
    }

    companion object {
        private const val RUB_CURRENCY_ISO = "RUB"
    }
}