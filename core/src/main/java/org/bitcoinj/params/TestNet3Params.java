/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
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
        port = 18333;
        maxTarget = Utils.decodeCompactBits(0x1e00ffffL);
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 239;
        genesisBlock.setTime(1429025142L);
        genesisBlock.setDifficultyTarget(0x1e00ffffL);
        genesisBlock.setNonce(6195114);
        spendableCoinbaseDepth = 100;

        // A script containing the difficulty bits and the following message:
        //   "DynamicCoin genesis / TestNet"
        CharSequence inputScriptHex = "04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73";
        genesisBlock = createGenesis(this, inputScriptHex);
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("000000c71f7f1f6bb21f4b834b3cdb22bf7bc289b093ad2136b0c2b5f9d3bc80"));

        alertSigningKey = Utils.HEX.decode("04302390343f91cc401d56d68b123028bf52e5fca1939df127f63c6467cdf9c8e2c14b61104cf817d0b780da337893ecc4aaff1309e536162dabbdb45200ca2b0a");

        checkpoints.put(0, new Sha256Hash("000000c71f7f1f6bb21f4b834b3cdb22bf7bc289b093ad2136b0c2b5f9d3bc80"));

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
