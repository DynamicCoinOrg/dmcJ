/**
 * Copyright 2011 Google Inc.
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

package org.bitcoinj.core;

import org.bitcoinj.params.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptOpCodes;
import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

import static org.bitcoinj.core.Coin.*;

/**
 * <p>NetworkParameters contains the data needed for working with an instantiation of a Bitcoin chain.</p>
 *
 * <p>This is an abstract class, concrete instantiations can be found in the params package. There are four:
 * one for the main network ({@link MainNetParams}), one for the public test network, and two others that are
 * intended for unit testing and local app development purposes. Although this class contains some aliases for
 * them, you are encouraged to call the static get() methods on each specific params class directly.</p>
 */
public abstract class NetworkParameters implements Serializable {
    /**
     * The protocol version this library implements.
     */
    public static final int PROTOCOL_VERSION = 70001;

    /**
     * The alert signing key
     */
    public static final byte[] ALERT_KEY = Utils.HEX.decode("04db94e84b7bc99965a368efb0a7b7773715ff61d46bbfa27ecc5f30572e9e50191b58b3c53bd6564e1f9b0a0041b6e45d5124516e475fbd85c4245b8f22213141");

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.dynamiccoin.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = "org.dynamiccoin.test";
    /** The string returned by getId() for regtest mode. */
    public static final String ID_REGTEST = "org.dynamiccoin.regtest";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "org.dynamiccoinj.unittest";

    /** The string used by the payment protocol to represent the main net. */
    public static final String PAYMENT_PROTOCOL_ID_MAINNET = "main";
    /** The string used by the payment protocol to represent the test net. */
    public static final String PAYMENT_PROTOCOL_ID_TESTNET = "test";

    // TODO: Seed nodes should be here as well.

    protected Block genesisBlock;
    protected BigInteger maxTarget;
    protected int port;
    protected long packetMagic;  // Indicates message origin network and is used to seek to the next message when stream state is unknown.
    protected int addressHeader;
    protected int p2shHeader;
    protected int dumpedPrivateKeyHeader;
    protected int interval;
    protected int targetTimespan;
    protected byte[] alertSigningKey;
    protected long liveFeedSwitchTime;
    protected long powSwitchHeight;

    /**
     * See getId(). This may be null for old deserialized wallets. In that case we derive it heuristically
     * by looking at the port number.
     */
    protected String id;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    protected int spendableCoinbaseDepth;

    protected int[] acceptableAddressCodes;
    protected String[] dnsSeeds;
    protected Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();
    private String liveFeedUrl; // TODO(dmc)

    protected NetworkParameters() {
        alertSigningKey = ALERT_KEY;
    }

    protected static Block createGenesis(NetworkParameters n, CharSequence inputScriptHex) {
        Block genesisBlock = new Block(n);
        Transaction t = new Transaction(n);
        try {
            byte[] bytes = Utils.HEX.decode(inputScriptHex);
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Utils.HEX.decode("04d5097dbafdba541ea1e7000f70190a066fe006fdbc807944d51cb6fe3fdab435c1c342b72fc7b4b9b34e4809e24a9f988b0039f27b62ef7ac298ebd4350c13f2"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, COIN.multiply(65535), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    public static final int TARGET_TIMESPAN = 3 * 60 * 60;  // 3 hours for difficulty retargeting window, on average.
    public static final int TARGET_SPACING = 15;  // 10 seconds per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;
    
    /**
     * Blocks with a timestamp after this should enforce BIP 16, aka "Pay to script hash". This BIP changed the
     * network rules in a soft-forking manner, that is, blocks that don't follow the rules are accepted but not
     * mined upon and thus will be quickly re-orged out as long as the majority are enforcing the rule.
     */
    public static final int BIP16_ENFORCE_TIME = 1333238400;
    
    /**
     * The maximum number of coins to be generated
     */
    public static final long MAX_COINS = Long.MAX_VALUE / COIN.value;

    /**
     * The maximum money to be generated
     */
    public static final Coin MAX_MONEY = COIN.multiply(MAX_COINS);

    /** Alias for TestNet3Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet() {
        return TestNet3Params.get();
    }

    /** Alias for TestNet2Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet2() {
        return TestNet2Params.get();
    }

    /** Alias for TestNet3Params.get(), use that instead. */
    @Deprecated
    public static NetworkParameters testNet3() {
        return TestNet3Params.get();
    }

    /** Alias for MainNetParams.get(), use that instead */
    @Deprecated
    public static NetworkParameters prodNet() {
        return MainNetParams.get();
    }

    /** Returns a testnet params modified to allow any difficulty target. */
    @Deprecated
    public static NetworkParameters unitTests() {
        return UnitTestParams.get();
    }

    /** Returns a standard regression test params (similar to unitTests) */
    @Deprecated
    public static NetworkParameters regTests() {
        return RegTestParams.get();
    }

    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    public abstract String getPaymentProtocolId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkParameters other = (NetworkParameters) o;
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** Returns the network parameters for the given string ID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromID(String id) {
        if (id.equals(ID_MAINNET)) {
            return MainNetParams.get();
        } else if (id.equals(ID_TESTNET)) {
            return TestNet3Params.get();
        } else if (id.equals(ID_UNITTESTNET)) {
            return UnitTestParams.get();
        } else if (id.equals(ID_REGTEST)) {
            return RegTestParams.get();
        } else {
            return null;
        }
    }

    /** Returns the network parameters for the given string paymentProtocolID or NULL if not recognized. */
    @Nullable
    public static NetworkParameters fromPmtProtocolID(String pmtProtocolId) {
        if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_MAINNET)) {
            return MainNetParams.get();
        } else if (pmtProtocolId.equals(PAYMENT_PROTOCOL_ID_TESTNET)) {
            return TestNet3Params.get();
        } else {
            return null;
        }
    }

    public int getSpendableCoinbaseDepth() {
        return spendableCoinbaseDepth;
    }

    /**
     * Returns true if the block height is either not a checkpoint, or is a checkpoint and the hash matches.
     */
    public boolean passesCheckpoint(int height, Sha256Hash hash) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash == null || checkpointHash.equals(hash);
    }

    /**
     * Returns true if the given height has a recorded checkpoint.
     */
    public boolean isCheckpoint(int height) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash != null;
    }

    /** Returns DNS names that when resolved, give IP addresses of active peers. */
    public String[] getDnsSeeds() {
        return dnsSeeds;
    }

    /**
     * <p>Genesis block for this chain.</p>
     *
     * <p>The first block in every chain is a well known constant shared between all Bitcoin implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     *
     * <p>The genesis blocks for both test and prod networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"The Times 03/Jan/2009 Chancellor on brink of second
     * bailout for banks"</i>.</p>
     */
    public Block getGenesisBlock() {
        return genesisBlock;
    }

    /** Default TCP port on which to connect to nodes. */
    public int getPort() {
        return port;
    }

    /** The header bytes that identify the start of a packet on this network. */
    public long getPacketMagic() {
        return packetMagic;
    }

    /**
     * First byte of a base58 encoded address. See {@link org.bitcoinj.core.Address}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public int getAddressHeader() {
        return addressHeader;
    }

    /**
     * First byte of a base58 encoded P2SH address.  P2SH addresses are defined as part of BIP0013.
     */
    public int getP2SHHeader() {
        return p2shHeader;
    }

    /** First byte of a base58 encoded dumped private key. See {@link org.bitcoinj.core.DumpedPrivateKey}. */
    public int getDumpedPrivateKeyHeader() {
        return dumpedPrivateKeyHeader;
    }

    /**
     * How much time in seconds is supposed to pass between "interval" blocks. If the actual elapsed time is
     * significantly different from this value, the network difficulty formula will produce a different value. Both
     * test and production Bitcoin networks use 2 weeks (1209600 seconds).
     */
    public int getTargetTimespan() {
        return targetTimespan;
    }

    /**
     * The version codes that prefix addresses which are acceptable on this network. Although Satoshi intended these to
     * be used for "versioning", in fact they are today used to discriminate what kind of data is contained in the
     * address and to prevent accidentally sending coins across chains which would destroy them.
     */
    public int[] getAcceptableAddressCodes() {
        return acceptableAddressCodes;
    }

    /**
     * If we are running in testnet-in-a-box mode, we allow connections to nodes with 0 non-genesis blocks.
     */
    public boolean allowEmptyPeerChain() {
        return true;
    }

    /** How many blocks pass between difficulty adjustment periods. Bitcoin standardises this to be 2015. */
    public int getInterval() {
        return interval;
    }

    /** Maximum target represents the easiest allowable proof of work. */
    public BigInteger getMaxTarget() {
        return maxTarget;
    }

    /**
     * The key used to sign {@link org.bitcoinj.core.AlertMessage}s. You can use {@link org.bitcoinj.core.ECKey#verify(byte[], byte[], byte[])} to verify
     * signatures using it.
     */
    public byte[] getAlertSigningKey() {
        return alertSigningKey;
    }

    public long getLiveFeedSwitchTime() { return liveFeedSwitchTime; }

    public String getLiveFeedUrl() { return liveFeedUrl; }
}
