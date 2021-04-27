package github.keboom;

import github.keboom.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author keboom
 * @date 2021/3/13 21:39
 */
@Slf4j
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {
    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("helloserviceImple收到：{}.", hello.getMessage());
        String result = "hello description is " + hello.getDescription();
        log.info("helloserviceimpl返回：{}.", result);
        return result;
    }
}
