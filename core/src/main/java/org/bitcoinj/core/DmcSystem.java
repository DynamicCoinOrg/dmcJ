package org.bitcoinj.core;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.utils.UsdExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class DmcSystem {
    private static final Logger log = LoggerFactory.getLogger(DmcSystem.class);

    // ok
    public static final Coin genesisReward  = Coin.COIN.multiply(65535);
    public static final Coin minReward      = Coin.COIN;
    public static final Coin maxReward      = Coin.COIN.multiply(1000000);
    public static final Usd  minTargetPrice = Usd.valueOf(1, 1);    // 1.01USD

    // ok
    private static final long mainChainGenesisRewardZone = 128000;
    private static final long mainChainDecreasingRewardZone = mainChainGenesisRewardZone + 1 + genesisReward.divide(Coin.COIN);

    private final AbstractBlockChain blockChain;
    private final GrsApi grsApi;
    private final NetworkParameters networkParameters;

    DmcSystem(String apiUrl, AbstractBlockChain blockChain, NetworkParameters networkParameters) throws DmcSystemException {
        try {
            this.grsApi = new GrsApi(apiUrl);
        } catch (MalformedURLException e) {
            throw new DmcSystemException("Malformed live feed URL in GRS API", e);
        }
        this.blockChain = blockChain;
        this.networkParameters = networkParameters;
    }

    // ok
    public boolean checkBlockReward(Block block, int height, Coin coinbaseValue, Coin fees) throws BlockStoreException {
        log.info("DmcSystem::checkBlockReward() : block.time = ", block.getTimeSeconds(),
                ", fees = ", fees.toString(), ", hash = ", block.getHashAsString(),
                ", height = ", height);

        BlockStore blockStore = blockChain.getBlockStore();

        if (coinbaseValue.isZero() || coinbaseValue.compareTo(fees) < 0) {
            throw new VerificationException("DmcSystem::checkBlockReward() : coinbase pays zero or <= fees (coinbase="
                                            + coinbaseValue.toString() + ", fees=" + fees.toString() + ")");
        }
        Coin blockReward = coinbaseValue.subtract(fees);

        //TODO(dmc): temporary simplification – one GetBlockReward result comparison check
        //            should be enough in the future (the second case of "if" construction)
        if (block.getTimeSeconds() > networkParameters.getLiveFeedSwitchTime()) {
            StoredBlock prevBlock = blockStore.get(block.getPrevBlockHash());
            Coin prevReward = prevBlock != null ? prevBlock.getReward() : genesisReward;
            Coin rewardDiff = Coin.valueOf(Math.abs(blockReward.subtract(prevReward).getValue()));
            if ((rewardDiff.isZero() || rewardDiff.equals(Coin.COIN))
                    && (blockReward.isGreaterEqualsThan(minReward) && blockReward.isLessEqualsThan(maxReward))) {
                return true;
            }
            throw new VerificationException("DmcSystem::checkBlockReward() : coinbase pays wrong (actual="
                                            + coinbaseValue.toString() + " vs mustbe="
                                            + getExpectedBlockReward(block, height).add(fees).toString() + ")");
        }

        if (!blockReward.equals(getExpectedBlockReward(block, height))) {
            throw new VerificationException("DmcSystem::checkBlockReward() : coinbase pays wrong reward (actual="
                                            + blockReward.toString() + " vs mustbe="
                                            + getExpectedBlockReward(block, height).toString() + ", fees="
                                            + fees.toString() + ")");
        }

        return true;
    }

    // ok
    public Coin getExpectedBlockReward(Block block, int height) throws BlockStoreException {
        log.info("DmcSystem::getExpectedBlockReward(): hash = ", block.getHashAsString(),
                ", height = ", height,
                ", time = ", block.getTimeSeconds());

        BlockStore blockStore = blockChain.getBlockStore();

        Coin expectedReward = Coin.COIN;

        if (block.getTimeSeconds() > networkParameters.getLiveFeedSwitchTime()) {
            StoredBlock prevBlock = blockStore.get(block.getPrevBlockHash());
            Coin prevReward = prevBlock != null ? prevBlock.getReward() : genesisReward;
            Coin reward = Coin.valueOf(prevReward.getValue());

            Usd price = getPrice(block.getTimeSeconds());
            Usd targetPrice = getTargetPrice(prevReward);

            if (price.isLessThan(targetPrice)) {
                reward = reward.subtract(Coin.COIN);
            } else if (price.isGreaterThan(targetPrice)) {
                reward = reward.add(Coin.COIN);
            }
            expectedReward = Coin.valueOf(Math.max(minReward.getValue(),
                                                    Math.min(reward.getValue(), maxReward.getValue())
            ));
        } else {

            if (networkParameters.getId().equals(NetworkParameters.ID_MAINNET)) {
                if (height >= 0 && height <= mainChainGenesisRewardZone) {
                    expectedReward = genesisReward;
                } else if (height > mainChainGenesisRewardZone && height < mainChainDecreasingRewardZone) {
                    expectedReward = Coin.COIN.multiply(mainChainDecreasingRewardZone - height);
                }

             } else {
                // TestNet
                expectedReward = Coin.COIN.multiply(1024);
             }
        }

        return expectedReward;
    }
//
//    CAmount CDmcSystem::GetBlockRewardForNewTip(unsigned int time)
//    {
//        LogPrintf("CDmcSystem::GetBlockRewardForNewTip: time=%d\n", time);
//    const CBlockIndex* tip = chainActive.Tip();
//
//        if (!tip) {
//            return genesisReward;
//        }
//
//        CAmount nSubsidy = 1 * COIN;
//
//        int nHeight = tip->nHeight + 1;
//
//        if (tip->nTime > Params().LiveFeedSwitchTime()) {
//            CAmount prevReward = tip->nReward;
//            CAmount reward     = prevReward;
//            unsigned int price = GetPrice(time);
//            CAmount target     = GetTargetPrice(prevReward);
//
//            if (price < target) {
//                reward -= 1 * COIN;
//            } else if (price > target) {
//                reward += 1 * COIN;
//            }
//            nSubsidy = std::max(minReward, std::min(reward, maxReward));
//        } else {
//            if (Params().NetworkID() == CBaseChainParams::MAIN) {
//            const int kGenesisRewardZone       = 128000;
//            const int kGenesisReward           = 65535;
//            const int kDecreasingRewardZone = kGenesisRewardZone + 1 + kGenesisReward;
//
//                if (nHeight >= 0 && nHeight <= kGenesisRewardZone) {
//                    nSubsidy = kGenesisReward * COIN;
//                } else if (nHeight > kGenesisRewardZone && nHeight < kDecreasingRewardZone) {
//                    nSubsidy = (kDecreasingRewardZone - nHeight) * COIN;
//                }
//            } else {
//                nSubsidy = 1024 * COIN;
//            }
//        }
//
//        LogPrintf("CDmcSystem::GetBlockRewardForNewTip: time=%d, nSubsidy=%d\n", time, nSubsidy);
//        return nSubsidy;
//    }

    // ok
    public Coin getBlockReward() {
        return blockChain.getChainHead().getReward();
    }

    // ok
    public Usd getPrice() {
        long latestBlockTime = blockChain.getChainHead().getHeader().getTimeSeconds();
        return grsApi.getPrice(latestBlockTime);
    }

    // ok
    public Usd getTargetPrice() {
        return getTargetPrice(blockChain.getChainHead().getReward());
    }

    // ok
    public Coin getTotalCoins() {
        return Coin.valueOf(blockChain.getChainHead().getChainReward().longValue());
    }

    // ok
    public Usd getMarketCap() {
        return getPrice().multiply(getTotalCoins().divide(Coin.COIN));
    }

    // ok
    protected Usd getPrice(long time) {
        return grsApi.getPrice(time);
    }

    // ok
    protected Usd getTargetPrice(long time) {
//        Coin reward = 0; // TODO(dmc): get block for time, and then its reward
//        return getTargetPrice(reward);
        //TODO(dmc): temporary simplification
        return Usd.CENT.multiply(10);
    }
//
//    CAmount CDmcSystem::GetTargetPrice(CAmount reward) const
//    {
//        CAmount targetPrice = 1 * USD1 + (reward * USD1) / (100 * COIN);
//
//        return std::max(minTargetPrice, targetPrice);
//    }
    protected Usd getTargetPrice(Coin reward) {
        UsdExchangeRate exchange = new UsdExchangeRate(Coin.COIN, Usd.valueOf(0,1));
        Usd targetPrice = Usd.valueOf(1, 0).add(exchange.coinToUsd(reward));    // TODO: !!!
        return Usd.valueOf(Math.max(minTargetPrice.getValue(), targetPrice.getValue()));
    }


    private class GrsApi {
        private JsonRpcHttpClient rpcClient;

        public GrsApi(String baseApiUrl) throws MalformedURLException {
            this.rpcClient = new JsonRpcHttpClient(
                    new URL(baseApiUrl));
        }

        public Usd getPrice(long time) {
            // projected prices for before-the-trading era
            if (time >= block0Time && time < block128002Time) {
                // genesis reward zone
                return Usd.valueOf(656,35);
            } else if (time >= block128002Time && time < block193536Time) {
                // decreasing reward zone
                return Usd.valueOf(0, 10);
            } else if (time >= block193536Time && time < networkParameters.getLiveFeedSwitchTime()) {
                return Usd.valueOf(0, 10);
            }

            // TODO(dmc): check cached price

            // get price from the live feed

            while (true) {  //TODO(dmc): !!!
                try {
//                    boost::this_thread::interruption_point();
                    long timestamp = 0; //TODO(dmc): must be 'time'
                    Price price = getGrsApiPrice(timestamp);
                    Usd usdPrice = Usd.valueOf(price.price);
                    log.info("GRS price for timestamp: time = ", price.time,
                                                   ", price = ", price.price,
                                                   ", usdPrice = ", usdPrice.toString());
                    return usdPrice;
                } catch (Throwable e) {
                    log.warn("Exception occured in rpcClient.invoke: ", e.getMessage());
                }
//                } catch (const boost::thread_interrupted& e) {
//                    LogPrintf("CGrsApi::GetPrice thread terminated\n");
//                    throw;
//                }
            }
        }

        // ok
        public Usd getLatestPrice() {
            return Usd.valueOf(0, 10);   // STUB: 0.1USD, TODO(dmc): get actual coin price
        }

        // nok
        public Price getGrsApiPrice(long timestamp) throws Throwable {
            if (timestamp == 0) {
                return rpcClient.invoke("price", null, Price.class);
            }
            return null;
        }

        private static final long block0Time      = 1438828878;
        private static final long block128002Time = 1440898409;
        private static final long block193536Time = 1441880383;

        private class Price {
            public long time;
            public long price;
        }
    }
}
