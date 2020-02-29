package me.xueyao.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Simon.Xue
 * @date 2019-12-13 00:59
 **/
@AllArgsConstructor
@Getter
public enum RStatus {
    SUCCESS(200),
    BAD_PARAM(400),
    SYSTEM(500);
    Integer code;
}
