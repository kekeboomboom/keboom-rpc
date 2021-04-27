package github.keboom.config;

import github.keboom.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import github.keboom.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author keboom
 * @date 2021/4/1 18:25
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        // addShutdownHook在jvm关闭前会执行该方法。
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            CuratorUtils.clearRegistry(CuratorUtils.getZkClient());
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
