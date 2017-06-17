package com.clover.remote.client.clovergo.di;


import com.clover.remote.client.clovergo.CloverGoConnectorImpl;
import com.firstdata.clovergo.data.SDKDataComponent;

import dagger.Component;

/**
 * Created by Arjun Chinya on 2/22/17.
 */
@ApplicationScope
@Component(dependencies = SDKDataComponent.class)
public interface ApplicationComponent {
    void inject(CloverGoConnectorImpl cloverGoConnectorImpl);
}
