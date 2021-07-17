# RPC

## 为什么要RPC？

对于很早以前我们的应用是单机应用，将应用部署在一台机器上，现在随着互联网的发展，我们的应用越来越庞大，越来越复杂。我们把不同应用部署在很多机器上，那么不同的机器间的交流沟通就需要网络来传送数据。那么我们每一次调用其他机器上的服务都需要进行一堆网络调用的代码，很是麻烦，代码也会很冗余。那么我们就将本地调用远程服务的过程的代码提取出来，这就有了基本的RPC的功能：本地可方便的调用远程服务。之后再有一些高级功能，如负载均衡，服务注册发现，服务监控等。

## 怎么做RPC？

1. 客户端调用远程服务，这样我们知道了服务名和参数
2. 我们对服务名和参数进行封装，通过动态代理来发送rpc请求，在发送之前我们需要知道IP地址，这里我们将服务注册到zk中，我们通过服务名在zk中得到IP列表，通过负载均衡得到合适的IP地址，进行网络调用
3. 进行网络传输，可以用socket或Netty，对于Netty我们可以设置编码解码器和序列化工具等
4. 服务端接收到请求，知道了服务名，可以通过反射创建类，调用相应的方法，返回响应
5. 客户端接收到响应

## 功能点

使用 Netty 作为网络传输，具有心跳机制，自定义编码器解 决 TCP 粘包拆包，可使用 kryo 或protostuff 做序列化工具。实现了 Zookeeper 的服务注册与发现，使 用一致性哈希做负载均衡。集成 Spring 通过注解进行注册服务和进行服务消费。使用动态代理屏蔽远程 方法调用细节。 

## version1.0

**client**：客户端要调用远程方法，需要用RpcClientProxy去动态代理。客户端将服务名，方法名和参数等给代理类。

**simple.RpcClientProxy**：使用JDK动态代理，在invoke方法中将封装RpcRequest。调用sendRpcrequest方法。

**simpe.RpcClient**：实现了sendRpcrequest方法，通过socket发送rpcRequest，并返回result。

**server**：启动server，并新建并注册服务。

**simple.RpcServer**：初始化线程池，创建serverSocket监听客户端连接，如果有新的客户端连接则作为任务在线程池中执行。

**simple.WorkThread**：实现具体的业务逻辑。通过socket接受RpcRequest，然后通过反射调用服务端的服务，然后将调用结果写入socket。RpcClient的sendRpcRequest方法接收到结果，向上return，直到client。

## version2.0

加入Netty，替代socket。使用kryo代替netty自带的编码解码器。

**NettyClientMain**：创建客户端代理类，通过代理类调用远程方法。

**RpcClientProxy**：封装RpcRequest，调用sendRpcRequest方法。

**NettyClientTransport**：实现了sendRpcRequest方法，获得channel，向channel写入RpcRequest，从AttributeKey中获得rpcResponse。

**ChannelProvider**：通过bootstrap监听客户端连接，并且实现了重试机制（通过递归和schedule实现）。

**NettyClientHandler**：channelRead方法，msg即为返回的rpcResponse，将其放入AttributeKey中。

**NettyServer**：初始化netty服务器，配置编码解码器和Handler。

**NettyServerHandler**：channelRead中使用线程池执行任务。msg为rpcRequest，调用rpcRequestHandler.handle获得结果，然后将结果写入channel。

**DefaultServiceRegistry**：使用ConcurrentHashMap作为注册中心。

**ThreadPoolFactory**：提出线程池创建工厂。



## Netty心跳

- [ ] 对于一个好的rpc，心跳要如何做才能高效呢？

客户端如果15秒没有write，则发送心跳

```java
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());
                RpcRequest rpcRequest = RpcRequest.builder().rpcMessageTypeEnum(RpcMessageTypeEnum.HEART_BEAT).build();
                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
```

userEventTriggered就是发送心跳的方法。

ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));客户端配置心跳时间



```java
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
```

ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));服务端配置心跳时间，30秒没有read到则断开连接。



## 集成spring

> @RpcService

此注解用来标注服务的实现类的，如**HelloServiceImpl**类。

此注解本身也为**@Component**。

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface RpcService {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}
```

与其相关的类：

```java
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;

    public SpringBeanPostProcessor() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // build RpcServiceProperties
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .group(rpcService.group()).version(rpcService.version()).build();
            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }
}

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                        .group(rpcReference.group()).version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
```

此类的作用为，对于每个bean被spring实例化，`postProcessBeforeInitialization`初始化前先判断当前类上面是否有**@RpcService**注解，如果有则获得注解的value，将其写入`rpcServiceProperties`，之后发布服务。

`postProcessAfterInitialization`每个bean初始化后，获得bean的fields，遍历fields，如果哪个field上面有`RpcReference`注解则创建客户端代理类，并将此field的值设置为代理类对象。

> @RpcScan

[component和componentScan](https://blog.csdn.net/neulily2005/article/details/83750027)

也就是说光有RpcService注解还不够，还需要让spring知道bean的位置在哪里。

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {

    String[] basePackage();

}
```

`ImportBeanDefinitionRegistrar`是一个扩展接口，通过此类可以自定义扫描注册到spring容器中。详细请看此文章：[通过ImportBeanDefinitionRegistrar动态注入Bean](https://cloud.tencent.com/developer/article/1551701)



## @SPI

- [ ] 问题：dubbo中如何使用SPI机制？？？如果说SPI是为了用户自定义自己的负载均衡，那么如何实现呢？

[Dubbo SPI可扩展机制](https://www.cnblogs.com/GrimMjx/p/10970643.html)

比如Protocol和LoadBalance我们通常会有自己的实现，这时dubbo提供可扩展机制，让我们方便的实现自己自定义的代理和负载均衡。



那么对于这个rpc项目，Javaguide是对于服务注册和发现标注了SPI注解，也就是说他希望我们自己实现服务的注册和发现。



原理：通过解析resource下的文件名和文件内容获得完整类路径，通过反射自然就可以相应的类了。



## @RpcReference

```java
@Component
public class HelloController {

    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("111", "222"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        assert "Hello description is 222".equals(hello);
        Thread.sleep(12000);
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222")));
        }
    }
}
```

此注解将自动完成clientProxy，NettyClientTransport，RpcServiceProperties创建。

> ~~ReferenceAnnotationBeanPostProcessor~~

~~首先我们看如何创建**NettyClientTransport**？~~

```java
    public ReferenceAnnotationBeanPostProcessor() {
        this.annotationTypes.add(RpcReference.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension("nettyClientTransport");
    }
```

~~在构造方法中，通过SPI来创建客户端运输类。~~

~~**ClientProxy**？**RpcServiceProperties**？~~

```java
    private class ReferenceFieldElement extends InjectionMetadata.InjectedElement {

        private String version;
        private String group;

        protected ReferenceFieldElement(Member member, String version, String group) {
            super(member, null);
            this.version = version;
            this.group = group;
        }

        @Override
        protected void inject(Object target, String requestingBeanName, PropertyValues pvs) throws Throwable {
            Field field = (Field) this.member;
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .group(group).version(version).build();
            RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
            Object value = rpcClientProxy.getProxy(field.getType());
            if (value != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(target, value);
            }
        }
    }
```

~~通过一个私有内部类，在inject方法中创建RpcServiceProperties和代理客户端。~~

~~//to do~~ 

~~暂时看不懂。。。。不需要看懂了，又改代码了。。。~~

ReferenceAnnotationBeanPostProcessor被删除。

请看**SpringBeanPostProcessor**：

获得**nettyClientTransport**：

```java
    public SpringBeanPostProcessor() {        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);        this.rpcClient = ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension("nettyClientTransport");    }
```

对每个bean的field字段进行遍历，如果发现有的field上面有RpcReference注解，则构造RpcServiceProperties，创建代理客户端。

```java
    @Override    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {        Class<?> targetClass = bean.getClass();        Field[] declaredFields = targetClass.getDeclaredFields();        for (Field declaredField : declaredFields) {            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);            if (rpcReference != null) {                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()                        .group(rpcReference.group()).version(rpcReference.version()).build();                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());                declaredField.setAccessible(true);                try {                    declaredField.set(bean, clientProxy);                } catch (IllegalAccessException e) {                    e.printStackTrace();                }            }        }        return bean;    }
```


nettyServerhandler的使用的线程是可以自定义的。

```java
        final DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(                RuntimeUtil.cpus() * 2,                new NamedThreadFactory("ServiceHandler")        );ch.pipeline().addLast(serviceHandlerGroup, new NettyServerHandler());
```

## 自定义编码解码器解决粘包拆包

`LengthFieldBasedFrameDecoder`解码器

我介绍一下流程：

1. 客户端发送请求，我们将其封装成rpcRequest对象
2. Netty客户端调用编码器，先将我们的一些头部信息放到bytebuf中，然后将对象的具体数据部分序列化成byte数组，然后将其放入bytebuf中，然后进行tcp发送，tcp会保证传送的有序可靠
3. tcp对于bytebuf过大则进行拆包，过小则进行粘包
4. 通过tcp协议我们以字节流形式发送到服务端，然后Netty服务端会将字节流封装成Netty的bytebuf
5. 在头部我们规定了content长度，在bytebuf读取完头部后，我们对剩下的内容数据进行反序列化得到对象



## 负载均衡

- [ ] 还有哪些负载均衡的算法？

### 一致性哈希

使用一致性哈希算法实现负载均衡。相关类：**ConsistentHashLoadBalance**。

```java
            for (String invoker : invokers) {                for (int i = 0; i < replicaNumber / 4; i++) {                    byte[] digest = md5(invoker + i);                    for (int h = 0; h < 4; h++) {                        long m = hash(digest, h);                        virtualInvokers.put(m, invoker);                    }                }            }        public String selectForKey(long hashCode) {            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();            if (entry == null) {                entry = virtualInvokers.firstEntry();            }            return entry.getValue();        }
```

一致性哈希原理是，对于0~2^32-1范围hash值，我们将他们从头到尾相连，组成一个hash环。在这个环上我们计算机器ip地址的hash值，以hash值为key，ip地址为value。然后计算数据的hash值放置环上，然后数据按顺时针旋转找到最近的机器的将数据存储其上。由于多个机器可能会聚集在一起导致数据分布不均，所以引入虚拟节点，所谓虚拟几点无非是通过hash函数计算出不同的hash值，将虚拟机器比较均匀的分布在环上。所谓虚拟的意思，就是最后还是将数据存储在“真实”机器上。当一个机器添加或者删除，只会涉及两个机器上的数据存储位置的改变。以上是一致性哈希做存储。



一致性哈希做负载均衡。以上面的代码实现做解释，用TreeMap做存储，将同一个ip地址分别以不同h的高度计算出hash值，也就是建立虚拟节点并将其较均匀分布在hash环上。key为哈希值，value为ip地址。ip地址就是“机器”。serviceName就是“数据”，计算serviceName的哈希值，通过tailMap.firstEntry方法（tailMap方法可以获得大于该哈希值的所有Entry，然后在调用firstEntry就可以获得离的最近的机器节点这两个方法模拟的就是一个数据节点顺时针存储到最近的一个机器节点），可以得到在hash环上顺时针最近的ip地址节点。



相关文章：[一致性哈希算法](https://zhuanlan.zhihu.com/p/129049724)

[dubbo的一致性哈希](https://blog.csdn.net/yuanshangshenghuo/article/details/107899085)

[dubbo中的一致性hash（ConsistentHashLoadBalance）详解](https://blog.csdn.net/lz710117239/article/details/79390670)



## JDK动态代理

在rpc框架中主要用来屏蔽某些重复代码和实现细节。比如封装rpcRequest并将其通过网络发送出去，接受响应。



## 序列化

- [ ] 两者序列化工具的区别？效率？使用场景？

### kryo

只是用Java语言。线程不安全，需要使用线程池或者ThreadLocal等。

### protostuff

谷歌开发，支持多种语言，线程安全。



## 注册中心

- [ ] zookeeper来做注册中心，还有其他方式么？
- [ ] 如果要实现一个高效的rpc，注册中心需要做哪些工作呢？



## 测试

- [ ] 如何测试我的rpc的效率？？？ 
