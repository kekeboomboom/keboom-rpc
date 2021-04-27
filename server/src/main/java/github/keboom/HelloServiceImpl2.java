package github.keboom;

import github.keboom.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author keboom
 * @date 2021/3/13 22:31
 */
@Slf4j
@RpcService(group = "test2", version = "version1")
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
