package com.hirshi001.tests.util;

import com.hirshi001.quicnetworking.helper.QuicNetworkingEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QuicNetworkingEnvironmentGroup<Channel extends Enum<Channel>, Priority extends Enum<Priority>> implements AutoCloseable{

    private final List<QuicNetworkingEnvironment<Channel, Priority>> resources = new ArrayList<>();

    public QuicNetworkingEnvironmentGroup(Collection<QuicNetworkingEnvironment<Channel, Priority>> resources) {
        this.resources.addAll(resources);
    }

    public QuicNetworkingEnvironment<Channel, Priority> get(int index) {
        return resources.get(index);
    }

    @Override
    public void close() {
        Exception primary = null;

        for (AutoCloseable r : resources) {
            try {
                r.close();
            } catch (Exception e) {
                if (primary == null) {
                    primary = e;
                } else {
                    primary.addSuppressed(e);
                }
            }
        }

        if (primary != null) {
            throw new RuntimeException(primary);
        }
    }
}
