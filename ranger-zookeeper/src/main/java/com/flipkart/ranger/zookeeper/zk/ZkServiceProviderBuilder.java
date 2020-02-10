package com.flipkart.ranger.zookeeper.zk;

import com.flipkart.ranger.core.model.NodeDataSource;
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.serviceprovider.BaseServiceProviderBuilder;
import com.flipkart.ranger.core.serviceprovider.ServiceProvider;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 *
 */
@Slf4j
public class ZkServiceProviderBuilder<T> extends BaseServiceProviderBuilder<T, ZkServiceProviderBuilder<T>> {
    private CuratorFramework curatorFramework;
    private String connectionString;


    public ZkServiceProviderBuilder<T> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public ZkServiceProviderBuilder<T> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    public ServiceProvider<T> build() {
        if (null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            log.info("Building custom curator framework");
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
            super.withStartSignalHandler(x -> curatorFramework.start());
            super.withStopSignalHandler(x -> curatorFramework.close());
        }
        return super.buildProvider();
    }

    @Override
    protected NodeDataSource<T> dataSource(Service service) {
        return new ZkNodeDataSource<>(service, curatorFramework);
    }
}
