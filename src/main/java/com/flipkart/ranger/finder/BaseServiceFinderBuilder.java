package com.flipkart.ranger.finder;

import com.flipkart.ranger.model.Deserializer;
import com.flipkart.ranger.model.ServiceNodeSelector;
import com.flipkart.ranger.model.ServiceRegistry;
import com.flipkart.ranger.model.ShardSelector;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public abstract class BaseServiceFinderBuilder<T, RegistryType extends ServiceRegistry<T>, FinderType extends ServiceFinder<T, RegistryType>> {
    private String namespace;
    private String serviceName;
    private CuratorFramework curatorFramework;
    private String connectionString;
    private Deserializer<T> deserializer;
    private ShardSelector<T, RegistryType> shardSelector;
    private ServiceNodeSelector<T> nodeSelector = new RandomServiceNodeSelector<T>();

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withNamespace(final String namespace) {
        this.namespace = namespace;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withDeserializer(Deserializer<T> deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withShardSelector(ShardSelector<T, RegistryType> shardSelector) {
        this.shardSelector = shardSelector;
        return this;
    }

    public BaseServiceFinderBuilder<T, RegistryType, FinderType> withNodeSelector(ServiceNodeSelector<T> nodeSelector) {
        this.nodeSelector = nodeSelector;
        return this;
    }

    public FinderType build() throws Exception {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(deserializer);
        if( null == curatorFramework) {
            Preconditions.checkNotNull(connectionString);
            curatorFramework = CuratorFrameworkFactory.builder()
                    .namespace(namespace)
                    .connectString(connectionString)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 100)).build();
        }
        Service service = new Service(curatorFramework, namespace, serviceName);
        return buildFinder(service, deserializer, shardSelector, nodeSelector);
    }

    protected abstract FinderType buildFinder(Service service,
                                              Deserializer<T> deserializer,
                                              ShardSelector<T, RegistryType> shardSelector,
                                              ServiceNodeSelector<T> nodeSelector);

}
