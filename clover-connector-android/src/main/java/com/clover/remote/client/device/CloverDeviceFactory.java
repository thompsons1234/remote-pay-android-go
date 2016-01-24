package com.clover.remote.client.device;

import com.clover.common2.clover.Clover;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CloverDeviceFactory
{
    public static CloverDevice get(final CloverDeviceConfiguration configuration)
    {
        String cloverDevicetypeName = configuration.getCloverDeviceTypeName();

        CloverDevice cd = null;
        try {
            Class cls = Class.forName(cloverDevicetypeName);
            Constructor<CloverDevice> ctor = cls.getConstructor(CloverDeviceConfiguration.class);
            cd = ctor.newInstance(configuration);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (ClassCastException cce) {
            cce.printStackTrace();
        } catch (InstantiationException ie) {
            ie.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        }
        return cd;
    }
}