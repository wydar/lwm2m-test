/*******************************************************************************
 * Copyright (c) 2016 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.integration.tests.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.eclipse.leshan.core.node.codec.DefaultLwM2mDecoder;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.model.StaticModelProvider;
import org.eclipse.leshan.server.redis.RedisRegistrationStore;
import org.eclipse.leshan.server.security.InMemorySecurityStore;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.Pool;

public class RedisIntegrationTestHelper extends IntegrationTestHelper {
    @Override
    public void createServer() {
        LeshanServerBuilder builder = new LeshanServerBuilder();
        StaticModelProvider modelProvider = new StaticModelProvider(createObjectModels());
        builder.setObjectModelProvider(modelProvider);
        DefaultLwM2mDecoder decoder = new DefaultLwM2mDecoder();
        builder.setDecoder(decoder);
        builder.setLocalAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        builder.setLocalSecureAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        builder.setSecurityStore(new InMemorySecurityStore());

        // Create redis store
        Pool<Jedis> jedis = createJedisPool();
        builder.setRegistrationStore(new RedisRegistrationStore(jedis));

        // Build server !
        server = builder.build();
        // monitor client registration
        setupServerMonitoring();
    }

    public Pool<Jedis> createJedisPool() {
        String redisURI = System.getenv("REDIS_URI");
        if (redisURI == null)
            redisURI = "";
        return new JedisPool(redisURI);
    }
}
