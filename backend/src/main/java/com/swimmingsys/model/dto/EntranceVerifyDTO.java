package com.swimmingsys.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 扫码验证请求DTO
 */
@Data
public class EntranceVerifyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 二维码令牌
     */
    @NotBlank(message = "二维码令牌不能为空")
    private String qrcodeToken;
}
