/*
 * Copyright 2013 Google Inc.
 * Copyright 2015 The DynamicCoin developers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.params;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.core.Block.BLOCK_VERSION_0_3;

/**
 * Parameters for the main production network on which people trade goods and services.
 */
public class MainNetParams extends NetworkParameters {

    public MainNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1e00ffffL);
        dumpedPrivateKeyHeader = 128;
        addressHeader = 0;
        p2shHeader = 5;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        port = 7333;
        packetMagic = 0xf9beb4e0L;
        genesisBlock.setVersion(BLOCK_VERSION_0_3);
        genesisBlock.setDifficultyTarget(0x1e00ffffL);
        genesisBlock.setTime(1438828878L);
        genesisBlock.setNonce(20475014);
        id = ID_MAINNET;
        spendableCoinbaseDepth = 100;
        liveFeedSwitchTime = 1469916000;
        powSwitchHeight = 2500000;

        // A script containing the difficulty bits and the following message:
        //
        //   "DynamicCoin genesis / MainNet"
        CharSequence inputScriptHex = "04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73";
        genesisBlock = createGenesis(this, inputScriptHex);
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("000000152106c2bd4678859ad548da538e2ca0a3ea15dc7c44b0c8bdd8eb5060"),
                   genesisHash);

        alertSigningKey = Utils.HEX.decode("04db94e84b7bc99965a368efb0a7b7773715ff61d46bbfa27ecc5f30572e9e50191b58b3c53bd6564e1f9b0a0041b6e45d5124516e475fbd85c4245b8f22213141");

        checkpoints.put(0, new Sha256Hash("000000152106c2bd4678859ad548da538e2ca0a3ea15dc7c44b0c8bdd8eb5060"));

        dnsSeeds = new String[] {
                "main.seeds.dynamiccoin.net"
        };
    }

    private static MainNetParams instance;
    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }
}
