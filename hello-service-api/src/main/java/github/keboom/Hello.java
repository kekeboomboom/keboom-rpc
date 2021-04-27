package github.keboom;

import lombok.*;

import java.io.Serializable;

/**
 * @author keboom
 * @date 2021/3/13 20:12
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
    private String message;
    private String description;
}
