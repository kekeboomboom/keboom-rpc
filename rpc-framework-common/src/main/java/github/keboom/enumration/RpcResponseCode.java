package github.keboom.enumration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author keboom
 * @date 2021/3/13 23:20
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCode {
    SUCCESS(200, "调用方法成功"),
    FAIL(500, "调用方法失败"),
    NOT_FOUND_METHOD(500, "未找到指定方法"),
    NOT_FOUND_CLASS(500, "未找到指定类");

    private final int code;

    private final String message;
}
