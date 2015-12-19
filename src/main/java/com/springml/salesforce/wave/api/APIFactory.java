package com.springml.salesforce.wave.api;

import com.springml.salesforce.wave.impl.ForceAPIImpl;
import com.springml.salesforce.wave.impl.WaveAPIImpl;
import com.springml.salesforce.wave.util.SFConfig;
import com.springml.salesforce.wave.util.WaveAPIConstants;

/**
 * Factory class to get WaveAPI
 */
public class APIFactory {
    private static APIFactory instance = null;

    private APIFactory() {}

    public static APIFactory getInstance() {
        if (instance == null) {
            instance = new APIFactory();
        }

        return instance;
    }

    public WaveAPI waveAPI(String username, String password, String loginURL) throws Exception {
        return this.waveAPI(username, password, loginURL, WaveAPIConstants.API_VERSION);
    }

    public WaveAPI waveAPI(String username, String password, String loginURL, String apiVersion) throws Exception {
        return new WaveAPIImpl(new SFConfig(username, password, loginURL, apiVersion));
    }

    public ForceAPI forceAPI(String username, String password, String loginURL) throws Exception {
        return this.forceAPI(username, password, loginURL, WaveAPIConstants.API_VERSION);
    }

    public ForceAPI forceAPI(String username, String password, String loginURL, String apiVersion) throws Exception {
        return new ForceAPIImpl(new SFConfig(username, password, loginURL, apiVersion));
    }
}
