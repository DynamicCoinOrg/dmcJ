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

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;

import java.math.BigInteger;

import static org.bitcoinj.core.Coin.COIN;

/**
 * Network parameters used by the bitcoinj unit tests (and potentially your own). This lets you solve a block using
 * {@link org.bitcoinj.core.Block#solve()} by setting difficulty to the easiest possible.
 */
public class UnitTestParams extends NetworkParameters {
    public UnitTestParams() {
        super();
        id = ID_UNITTESTNET;
        packetMagic = 0x0b110908;
        addressHeader = 111;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        maxTarget = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0", 16);

        genesisReward = COIN.multiply(1);
        CharSequence inputScriptHex = "0004ffff001e01041d44796e616d6963436f696e2067656e65736973202f20546573744e6574";
        genesisBlock = createGenesis(this, inputScriptHex);
        genesisBlock.setTime(System.currentTimeMillis() / 1000);
        genesisBlock.setDifficultyTarget(Utils.encodeCompactBits(new BigInteger("0fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16))/*Block.EASIEST_DIFFICULTY_TARGET*/);
        genesisBlock.solve();
        port = 17333;
        interval = 10;
        dumpedPrivateKeyHeader = 239;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        spendableCoinbaseDepth = 5;
        dnsSeeds = null;
    }

    private static UnitTestParams instance;
    public static synchronized UnitTestParams get() {
        if (instance == null) {
            instance = new UnitTestParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return null;
    }
}
