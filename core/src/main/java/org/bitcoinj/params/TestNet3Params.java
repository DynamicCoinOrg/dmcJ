/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
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
import static org.bitcoinj.core.Coin.COIN;

/**
 * Parameters for the testnet, a separate public instance of Bitcoin that has relaxed rules suitable for development
 * and testing of applications and new Bitcoin versions.
 */
public class TestNet3Params extends NetworkParameters {
    public TestNet3Params() {
        super();
        id = ID_TESTNET;
        packetMagic = 0x0b110908;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1e00ffffL);
        port = 17333;
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 239;
        spendableCoinbaseDepth = 100;
        genesisReward = COIN.multiply(1024);

        // A script containing the difficulty bits and the following message:
        //   "DynamicCoin genesis / TestNet"
        CharSequence inputScriptHex = "0004ffff001e01041d44796e616d6963436f696e2067656e65736973202f20546573744e6574";
        genesisBlock = createGenesis(this, inputScriptHex);
        genesisBlock.setVersion(BLOCK_VERSION_0_3);
        genesisBlock.setTime(1436316222L);
        genesisBlock.setDifficultyTarget(0x1e00ffffL);
        genesisBlock.setNonce(42228550);
        String genesisHash = genesisBlock.getHashAsString();

        checkState(genesisBlock.getMerkleRoot().toString().equals("5cf528874737db2abdcfc5be9093b172db4820672c1215e30142dbf1710b8b59"));
        checkState(genesisHash.equals("0000008946caf55a2d30cde53852cf23c82ead9116b2af84b896ae79b3e40282"));

        alertSigningKey = Utils.HEX.decode("04302390343f91cc401d56d68b123028bf52e5fca1939df127f63c6467cdf9c8e2c14b61104cf817d0b780da337893ecc4aaff1309e536162dabbdb45200ca2b0a");

        checkpoints.put(0, new Sha256Hash("0000008946caf55a2d30cde53852cf23c82ead9116b2af84b896ae79b3e40282"));

        dnsSeeds = new String[] {
                "test.seeds.dynamiccoin.org"
        };
    }

    private static TestNet3Params instance;
    public static synchronized TestNet3Params get() {
        if (instance == null) {
            instance = new TestNet3Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }
}
